use bluer::{Adapter, AdapterEvent, DeviceEvent, Session};
use futures::{pin_mut, stream::SelectAll, StreamExt};
use log::info;
use tokio::time::{sleep, Duration};

use self::jni::objects::JClass;
use self::jni::JNIEnv;
use jni;
use jni::objects::JValue;

use super::{bt_manager, rt_handle};

pub(crate) struct BlueManager {
    pub(crate) session: Session,
    pub(crate) adapter: Adapter,
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_init<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) {
    info!("Initializing BluetoothManager");
    let _ = bt_manager();
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_discover<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    view_model_class: JClass<'local>,
) {
    info!("BlueManager::discover()");

    let handle = rt_handle();
    let devices = handle.block_on(discover_devices());

    let class = env.find_class("java/lang/String").unwrap();
    let initial = env.new_string("").unwrap();
    let array = env
        .new_object_array(devices.len() as i32, class, initial)
        .unwrap();

    for (i, d) in devices.iter().enumerate() {
        let _ = env.set_object_array_element(&array, i as i32, env.new_string(d).unwrap());
    }

    let _ = env.call_method(
        view_model_class,
        "discoveredDevicesHandler",
        "([Ljava/lang/String;)V",
        &[JValue::from(&array)],
    );
}

async fn discover_devices() -> Vec<String> {
    let mut devices: Vec<String> = Vec::new();
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

    loop {
        tokio::select! {
            Some(device_event) = device_events.next() => {
                    match device_event {
                        AdapterEvent::DeviceAdded(addr) => {
                            let device = manager.adapter.device(addr).expect("Getting device should not fail");
                            let device_name = device.name().await.expect("Getting device name should not fail").unwrap_or(addr.to_string());
                            info!("Device ({}) with address: {} added", device_name, addr);
                            devices.push(device_name);

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
            _ = sleep(Duration::from_secs(12)) => {
                info!("Timeout reached, ending discovery");
                break;
            }
            else => {
                info!("tokio::select! => else");
                break;
            }
        }
    }

    manager
        .adapter
        .set_powered(false)
        .await
        .expect("Turning off bluetooth should not fail");

    devices
}
