use bluer::{Adapter, AdapterEvent, Address, DeviceEvent, Session};
use futures::{pin_mut, stream::SelectAll, StreamExt};
use log::info;
use std::collections::HashMap;
use tokio::time::{sleep, Duration};

use self::jni::objects::JClass;
use self::jni::JNIEnv;
use jni::objects::JValue;
use jni::{self, objects::JString};

use super::{bt_manager, rt_handle};

pub(crate) struct BlueManager {
    pub(crate) session: Session,
    pub(crate) adapter: Adapter,
    pub(crate) device_addrs: HashMap<String, Address>,
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
    let mut manager = bt_manager();
    manager.device_addrs.clear();

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
                            devices.push(device_name.clone());
                            manager.device_addrs.insert(device_name, device.address());

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

    devices
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_BlueManager_connectToDevice<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    device_name: JString<'local>,
) {
    let device_name: String = env
        .get_string(&device_name)
        .expect("Getting String from env should not fail")
        .into();

    let manager = bt_manager();
    let device_addr = manager
        .device_addrs
        .get(&device_name)
        .expect("Device should still be available");
    let device = manager
        .adapter
        .device(*device_addr)
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
