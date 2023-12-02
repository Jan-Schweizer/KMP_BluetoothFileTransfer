#[allow(non_snake_case)]
mod desktop {
    use bluer::{AdapterEvent, DeviceEvent, DiscoveryFilter, Session};
    use env_logger::Env;
    use futures::{pin_mut, stream::SelectAll, StreamExt};
    use log::info;
    use tokio::runtime::Runtime;
    use tokio::time::{sleep, Duration};

    use self::jni::objects::JClass;
    use self::jni::JNIEnv;
    use jni;
    use jni::objects::JValue;

    #[no_mangle]
    pub extern "system" fn Java_de_schweizer_bft_BlueManager_initLogger<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) {
        env_logger::Builder::from_env(Env::default().default_filter_or("info")).init();
        info!("Desktop Logger initialized")
    }

    #[no_mangle]
    pub extern "system" fn Java_de_schweizer_bft_BlueManager_discover<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        view_model_class: JClass<'local>,
    ) {
        info!("BlueManager::discover()");

        let mut devices: Vec<String> = Vec::new();

        let block = async {
            let session = Session::new()
                .await
                .expect("Creating bluer Session should not fail");
            let adapter = session
                .default_adapter()
                .await
                .expect("Creating bluer Adapter should not fail");
            info!(
                "Discovering devices using Bluetooth adapter {}\n",
                adapter.name()
            );
            adapter
                .set_powered(true)
                .await
                .expect("Turning on bluetooth should not fail");
            let filter = DiscoveryFilter {
                transport: bluer::DiscoveryTransport::BrEdr,
                ..Default::default()
            };
            adapter
                .set_discovery_filter(filter)
                .await
                .expect("Setting bluer discovery filter should not fail");
            info!(
                "Using discovery filter:\n{:#?}\n\n",
                adapter.discovery_filter().await
            );

            let device_events = adapter
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
                                    let device = adapter.device(addr).expect("Getting device should not fail");
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
        };

        let tokio_rt = Runtime::new().expect("Creating Tokio Runtime should not fail");
        let handle = tokio_rt.handle();
        handle.block_on(block);

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
}
