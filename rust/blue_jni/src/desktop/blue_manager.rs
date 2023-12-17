use std::str::FromStr;

use bluer::{Adapter, AdapterEvent, Address, DeviceEvent, Session};
use futures::{pin_mut, stream::SelectAll, StreamExt};
use log::info;
use tokio::sync::mpsc;
use tokio::time::{sleep, Duration};

use self::jni::objects::JClass;
use self::jni::JNIEnv;
use jni::objects::JValue;
use jni::{self, objects::JString};

use super::{bt_manager, rt_handle};

pub(crate) struct BlueManager {
    pub(crate) _session: Session,
    pub(crate) adapter: Adapter,
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_init<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) {
    info!("Initializing BluetoothManager");
    drop(bt_manager());
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_discover<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    view_model_class: JClass<'local>,
) {
    info!("BlueManager::discover()");

    let handle = rt_handle();
    handle.block_on(discover_devices(env, view_model_class));
}

async fn discover_devices<'local>(mut env: JNIEnv<'local>, view_model_class: JClass<'local>) {
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

    let (tx, mut rx) = mpsc::channel(1);
    let duration = Duration::from_secs(12);
    tokio::spawn(sleep_for(duration, tx));

    loop {
        tokio::select! {
            Some(device_event) = device_events.next() => {
                    match device_event {
                        AdapterEvent::DeviceAdded(addr) => {
                            let device = manager.adapter.device(addr).expect("Getting device should not fail");
                            let device_name = device.name().await.expect("Getting device name should not fail").unwrap_or(addr.to_string());
                            let addr_str = addr.to_string();
                            info!("Device ({}) with address: {} added", device_name, addr_str);

                            device_discovered(&mut env, &view_model_class, &device_name, &addr_str);

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
            _ = rx.recv() => {
                info!("Timeout reached, ending discovery");
                discovery_stopped(&mut env, &view_model_class);
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
    _class: JClass<'local>,
    device_addr: JString<'local>,
) {
    let manager = bt_manager();

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
    _class: JClass<'local>,
) {
    info!("Cancellation not yet implemented");
}

async fn sleep_for(duration: Duration, tx: mpsc::Sender<()>) {
    sleep(duration).await;
    let _ = tx.send(());
}

fn device_discovered<'local>(
    env: &mut JNIEnv<'local>,
    view_model_class: &JClass<'local>,
    device: &str,
    addr: &str,
) {
    let device_name = env.new_string(&device).unwrap();
    let device_addr = env.new_string(&addr).unwrap();

    let _ = env.call_method(
        view_model_class,
        "onDeviceDiscovered",
        "(Ljava/lang/String;Ljava/lang/String;)V",
        &[JValue::from(&device_name), JValue::from(&device_addr)],
    );
}

fn discovery_stopped<'local>(env: &mut JNIEnv<'local>, view_model_class: &JClass<'local>) {
    let _ = env.call_method(view_model_class, "onDiscoveryStopped", "()V", &[]);
}
