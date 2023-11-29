#[allow(non_snake_case)]
mod desktop {
    use btleplug::api::{Central, Manager as _, Peripheral, ScanFilter};
    use btleplug::platform::Manager;
    use env_logger::Env;
    use jni::objects::JValue;
    use log::info;
    use tokio::runtime::Runtime;

    use self::jni::objects::JClass;
    use self::jni::JNIEnv;
    use jni;

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
            let manager = Manager::new()
                .await
                .expect("Creating a btleplug Manager should not fail");

            let adapters = manager
                .adapters()
                .await
                .expect("Getting adapters should not fail");

            if adapters.is_empty() {
                info!("No Bluetooth adapters!");
            } else {
                for adapter in &adapters {
                    info!("adapter: {:?}", adapter);
                }
            }

            let central = adapters
                .first()
                .expect("At least one Adapter should be available");
            info!("adapter info: {}", central.adapter_info().await.unwrap());

            let _ = central.start_scan(ScanFilter::default()).await;

            tokio::time::sleep(tokio::time::Duration::from_secs(2)).await;

            let peripherals = central.peripherals().await.unwrap();
            if peripherals.is_empty() {
                info!("No Peripherals found");
            } else {
                for p in &peripherals {
                    if let Some(name) = p.properties().await.unwrap().unwrap().local_name {
                        info!("peripheral: {}", name);
                        devices.push(name)
                    }
                }
            }

            let _ = central.stop_scan().await;
        };

        let tokio_rt = Runtime::new().expect("Creating Tokio Runtime should not fail");
        let handle = tokio_rt.handle();
        let _adapters = handle.block_on(block);

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
