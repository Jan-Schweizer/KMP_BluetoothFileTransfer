use std::str::FromStr;

use bluer::DiscoveryFilter;
use bluer::{Adapter, AdapterEvent, Address, DeviceEvent, Session, SessionEvent};
use futures::{pin_mut, stream::SelectAll, StreamExt};
use lazy_static::lazy_static;
use log::{info, warn};
use tokio::sync::{mpsc, Mutex};
use tokio::time::{sleep, Duration};

use jni::objects::{JObject, JString, JValue};
use jni::{Executor, JNIEnv};
use util::CommandConfig;

use crate::desktop::GLOBAL_JVM;

use super::{bt_manager, rt_handle};

#[derive(Clone, Debug)]
pub(crate) struct BlueManager {
    pub(crate) session: Session,
    pub(crate) adapter: Option<Adapter>,
}

impl BlueManager {
    pub(crate) async fn set_discovery_filter(&self) {
        if let Some(adapter) = &self.adapter {
            info!(
                "Discovering devices using Bluetooth adapter {}\n",
                adapter.name()
            );
            let filter = DiscoveryFilter {
                transport: bluer::DiscoveryTransport::BrEdr,
                ..Default::default()
            };
            adapter
                .set_discovery_filter(filter)
                .await
                .expect("Setting bluer discovery filter should not fail");
            info!(
                "Using discovery filter:\n{:#?}\n",
                adapter.discovery_filter().await
            );
        }
    }
}

#[derive(Debug)]
struct BlueState {
    timeout: Option<mpsc::Sender<()>>,
    cancel: Option<mpsc::Sender<()>>,
}

impl BlueState {
    fn new() -> Self {
        Self {
            timeout: None,
            cancel: None,
        }
    }

    fn set(timeout: mpsc::Sender<()>, cancel: mpsc::Sender<()>) -> Self {
        Self {
            timeout: Some(timeout),
            cancel: Some(cancel),
        }
    }
}

lazy_static! {
    static ref BLUE_STATE: Mutex<BlueState> = Mutex::new(BlueState::new());
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_init<'local>(
    _env: JNIEnv<'local>,
    _obj: JObject<'local>,
) {
    info!("Initializing BluetoothManager");
    // Ensure BlueManager is initialized synchronously
    bt_manager();

    rt_handle().block_on(async {
        let manager = bt_manager().lock().await;
        update_bluetooth_enabled(manager.adapter.is_some());
    });

    rt_handle().spawn(bluetooth_adapter_events());
}

async fn bluetooth_adapter_events() {
    let manager = bt_manager().lock().await;
    let session_events = manager
        .session
        .events()
        .await
        .expect("Getting bluettooth session events should not fail");
    pin_mut!(session_events);
    drop(manager);

    loop {
        tokio::select! {
            Some(session_event) = session_events.next() => {
                let mut manager = bt_manager().lock().await;
                match session_event {
                    SessionEvent::AdapterAdded(adapter_name) => {
                        info!("Adapter added: {adapter_name}");
                        match manager.session.adapter(&adapter_name) {
                            Ok(adapter) => {
                                manager.adapter = Some(adapter);
                                manager.set_discovery_filter().await;
                                update_bluetooth_enabled(true);
                            }
                            Err(err) => {
                                warn!("Error: {err}. Adapter {adapter_name} could not be retrieved");
                                manager.adapter = None;
                                update_bluetooth_enabled(false);
                            }
                        }
                    }
                    SessionEvent::AdapterRemoved(adapter) => {
                        info!("Adapter removed: {adapter}");
                        manager.adapter = None;
                        update_bluetooth_enabled(false);
                        cancel_disocovery().await;
                    }
                }
                drop(manager);
            }
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_discover<'local>(
    _env: JNIEnv<'local>,
    _obj: JObject<'local>,
) {
    info!("BlueManager::discover()");

    rt_handle().spawn(discover_devices());
}

async fn discover_devices() {
    let manager = bt_manager().lock().await;
    let adapter = if let Some(adapter) = &manager.adapter {
        adapter
    } else {
        warn!("Cannot start discovery because no bluetooth adapter is available");
        return;
    };

    let device_events = adapter
        .discover_devices()
        .await
        .expect("Discovering devices should not fail");
    pin_mut!(device_events);
    drop(manager);

    let mut all_change_events = SelectAll::new();

    let (timeout_tx, mut timeout_rx) = mpsc::channel(1);
    let (cancel_tx, mut cancel_rx) = mpsc::channel(1);
    *BLUE_STATE.lock().await = BlueState::set(timeout_tx, cancel_tx);

    let duration = Duration::from_secs(12);
    let timeout_task = tokio::spawn(sleep_and_notify(duration));

    loop {
        tokio::select! {
            Some(device_event) = device_events.next() => {
                    match device_event {
                        AdapterEvent::DeviceAdded(addr) => {
                            let manager = bt_manager().lock().await;
                            let adapter = manager.adapter.as_ref().unwrap();
                            let device = adapter.device(addr).expect("Getting device should not fail");
                            drop(manager);

                            let device_name = device.name().await.expect("Getting device name should not fail").unwrap_or(addr.to_string());
                            let addr_str = addr.to_string();
                            info!("Device ({}) with address: {} added", device_name, addr_str);

                            device_discovered(&device_name, &addr_str);

                            let change_event = device.events().await.expect("Getting events from device should not fail").map(move |event| (addr, event));
                            all_change_events.push(change_event);
                        }
                        AdapterEvent::DeviceRemoved(addr) => {
                            info!("Device removed: {addr}");
                        }
                        _ => (),
                    }
            }
            Some((addr, DeviceEvent::PropertyChanged(prop))) = all_change_events.next() => {
                info!("Device changed: {}", addr);
                info!("    {:?}", prop);
            }
            Some(()) = timeout_rx.recv() => {
                info!("Timeout reached, ending discovery");
                discovery_stopped().await;
                break;
            }
            Some(()) = cancel_rx.recv() => {
                info!("Canceling Discovery");
                discovery_stopped().await;
                timeout_task.abort();
                break;
            }
            else => {
                info!("tokio::select! => else");
                break;
            }
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_connectToDevice<'local>(
    mut env: JNIEnv<'local>,
    _obj: JObject<'local>,
    device_addr: JString<'local>,
) {
    info!("BlueManager::connectToDevice()");

    let device_addr: String = env
        .get_string(&device_addr)
        .expect("Getting String from env should not fail")
        .into();
    let device_addr = Address::from_str(&device_addr).unwrap();
    rt_handle().spawn(connect_to_device(device_addr));
}

async fn connect_to_device(device_address: Address) {
    let manager = bt_manager().lock().await;
    let adapter = if let Some(adapter) = &manager.adapter {
        adapter
    } else {
        warn!("Cannot connect to device because no bluetooth adapter is available");
        return;
    };

    let device = adapter
        .device(device_address)
        .expect("Device should still be available from adapter");
    drop(manager);

    let block = async {
        let props = device.all_properties().await.unwrap();
        props
    };
    let handle = rt_handle();
    let props = handle.block_on(block);
    for prop in props {
        info!("    {:?}", &prop);
    }
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_cancelDiscovery<'local>(
    _env: JNIEnv<'local>,
    _obj: JObject<'local>,
) {
    info!("BlueManager::cancelDiscovery()");

    rt_handle().spawn(cancel_disocovery());
}

async fn cancel_disocovery() {
    let state = BLUE_STATE.lock().await;
    if let Some(cancel) = &state.cancel {
        let _ = cancel.send(()).await;
    }
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_requestEnableBluetooth<'local>(
    _env: JNIEnv<'local>,
    _obj: JObject<'local>,
) {
    info!("BlueManager::requestEnableBluetooth()");

    request_enable_bluetooth();
}

fn request_enable_bluetooth() {
    let command = CommandConfig {
        command: "rfkill",
        args: vec!["toggle", "bluetooth"],
    };
    util::run_command(&command);
}

async fn sleep_and_notify(duration: Duration) {
    sleep(duration).await;

    let state = BLUE_STATE.lock().await;

    if let Some(timeout) = &state.timeout {
        let _ = timeout.send(()).await;
    }
}

fn device_discovered(device: &str, addr: &str) {
    let exec = Executor::new(GLOBAL_JVM.get().unwrap().clone());
    let _ = exec.with_attached(|env| {
        let device_name = env.new_string(&device).unwrap();
        let device_addr = env.new_string(&addr).unwrap();

        let blue_manager_cls = env
            .find_class("de/schweizer/bft/BlueManager")
            .expect("BlueManger could not be found");

        env.call_static_method(
            blue_manager_cls,
            "onDeviceDiscovered",
            "(Ljava/lang/String;Ljava/lang/String;)V",
            &[JValue::from(&device_name), JValue::from(&device_addr)],
        )
        .unwrap()
        .v()
    });
}

async fn discovery_stopped() {
    let exec = Executor::new(GLOBAL_JVM.get().unwrap().clone());
    let _ = exec.with_attached(|env| {
        let blue_manager_cls = env
            .find_class("de/schweizer/bft/BlueManager")
            .expect("BlueManger could not be found");

        env.call_static_method(blue_manager_cls, "onDiscoveryStopped", "()V", &[])
            .unwrap()
            .v()
    });

    *BLUE_STATE.lock().await = BlueState::new();
}

fn update_bluetooth_enabled(enabled: bool) {
    let exec = Executor::new(GLOBAL_JVM.get().unwrap().clone());
    let _ = exec.with_attached(|env| {
        let blue_manager_cls = env
            .find_class("de/schweizer/bft/BlueManager")
            .expect("BlueManger could not be found");

        env.call_static_method(
            blue_manager_cls,
            "updateBluetoothEnabled",
            "(Z)V",
            &[JValue::from(enabled)],
        )
        .unwrap()
        .v()
    });
}
