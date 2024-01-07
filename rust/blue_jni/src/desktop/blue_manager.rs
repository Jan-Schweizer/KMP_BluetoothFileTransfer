use std::str::FromStr;

use bluer::{Adapter, AdapterEvent, Address, DeviceEvent, Session};
use futures::{pin_mut, stream::SelectAll, StreamExt};
use lazy_static::lazy_static;
use log::info;
use tokio::sync::{mpsc, Mutex};
use tokio::time::{sleep, Duration};

use jni::objects::{GlobalRef, JObject, JString, JValue};
use jni::{Executor, JNIEnv};

use crate::desktop::GLOBAL_JVM;

use super::{bt_manager, rt_handle};

#[derive(Clone, Debug)]
pub(crate) struct BlueManager {
    pub(crate) _session: Session,
    pub(crate) adapter: Adapter,
}

#[derive(Debug)]
struct BlueState {
    timeout: Option<mpsc::Sender<()>>,
    cancel: Option<mpsc::Sender<()>>,
}

impl BlueState {
    fn new(timeout: mpsc::Sender<()>, cancel: mpsc::Sender<()>) -> Self {
        Self {
            timeout: Some(timeout),
            cancel: Some(cancel),
        }
    }

    fn reset() -> Self {
        Self {
            timeout: None,
            cancel: None,
        }
    }
}

lazy_static! {
    static ref BLUE_STATE: Mutex<BlueState> = Mutex::new(BlueState::reset());
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_init<'local>(
    _env: JNIEnv<'local>,
    _obj: JObject<'local>,
) {
    info!("Initializing BluetoothManager");
    drop(bt_manager().clone());
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_discover<'local>(
    env: JNIEnv<'local>,
    _obj: JObject<'local>,
    view_model: JObject<'local>,
) {
    info!("BlueManager::discover()");

    let view_model = env.new_global_ref(view_model).unwrap();

    rt_handle().spawn(discover_devices(view_model));
}

async fn discover_devices(view_model: GlobalRef) {
    let manager = bt_manager();

    manager
        .adapter
        .set_powered(true)
        .await
        .expect("Turning on bluetooth should not fail");

    let device_events = manager
        .adapter
        .discover_devices()
        .await
        .expect("Discovering devices should not fail");
    pin_mut!(device_events);

    let mut all_change_events = SelectAll::new();

    let (timeout_tx, mut timeout_rx) = mpsc::channel(1);
    let (cancel_tx, mut cancel_rx) = mpsc::channel(1);
    *BLUE_STATE.lock().await = BlueState::new(timeout_tx, cancel_tx);

    let duration = Duration::from_secs(12);
    let timeout_task = tokio::spawn(sleep_and_notify(duration));

    loop {
        tokio::select! {
            Some(device_event) = device_events.next() => {
                    match device_event {
                        AdapterEvent::DeviceAdded(addr) => {
                            let device = manager.adapter.device(addr).expect("Getting device should not fail");
                            let device_name = device.name().await.expect("Getting device name should not fail").unwrap_or(addr.to_string());
                            let addr_str = addr.to_string();
                            info!("Device ({}) with address: {} added", device_name, addr_str);

                            device_discovered(view_model.clone(), &device_name, &addr_str);

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
                discovery_stopped(view_model.clone()).await;
                break;
            }
            Some(()) = cancel_rx.recv() => {
                info!("Canceling Discovery");
                discovery_stopped(view_model.clone()).await;
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
    let manager = bt_manager();

    // TODO: Use executor
    let device_addr: String = env
        .get_string(&device_addr)
        .expect("Getting String from env should not fail")
        .into();
    let device_addr = Address::from_str(&device_addr).unwrap();

    let device = manager
        .adapter
        .device(device_addr)
        .expect("Device should still be available from adapter");

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
    rt_handle().spawn(async {
        let state = BLUE_STATE.lock().await;
        if let Some(cancel) = &state.cancel {
            let _ = cancel.send(()).await;
        }
    });
}

async fn sleep_and_notify(duration: Duration) {
    sleep(duration).await;

    let state = BLUE_STATE.lock().await;

    if let Some(timeout) = &state.timeout {
        let _ = timeout.send(()).await;
    }
}

fn device_discovered(view_model: GlobalRef, device: &str, addr: &str) {
    let exec = Executor::new(GLOBAL_JVM.get().unwrap().clone());
    let _ = exec.with_attached(|env| {
        let device_name = env.new_string(&device).unwrap();
        let device_addr = env.new_string(&addr).unwrap();

        env.call_method(
            view_model.as_obj(),
            "onDeviceDiscovered",
            "(Ljava/lang/String;Ljava/lang/String;)V",
            &[JValue::from(&device_name), JValue::from(&device_addr)],
        )
        .unwrap()
        .v()
    });
}

async fn discovery_stopped(view_model: GlobalRef) {
    let exec = Executor::new(GLOBAL_JVM.get().unwrap().clone());
    let _ = exec.with_attached(|env| {
        env.call_method(view_model.as_obj(), "onDiscoveryStopped", "()V", &[])
            .unwrap()
            .v()
    });

    *BLUE_STATE.lock().await = BlueState::reset();
}
