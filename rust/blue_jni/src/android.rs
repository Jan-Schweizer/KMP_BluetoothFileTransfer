#[allow(non_snake_case)]
pub mod android {
    use android_logger::Config;
    use btleplug::api::{
        bleuuid::BleUuid, Central, CentralEvent, Manager as _, Peripheral, ScanFilter,
    };
    use btleplug::platform::{init, Manager};
    use log::{info, LevelFilter};
    use tokio::runtime::Runtime;
    use tokio_stream::StreamExt;

    use self::jni::objects::{JClass, JString};
    use self::jni::JNIEnv;
    use jni;

    #[no_mangle]
    pub extern "system" fn Java_de_schweizer_bft_BlueManager_initLogger<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) {
        android_logger::init_once(
            Config::default()
                .with_max_level(LevelFilter::Trace)
                .with_tag("Rust"),
        );
        info!("Android Logger initialized");

        let _ = init(&env);
    }

    #[no_mangle]
    pub extern "system" fn Java_de_schweizer_bft_BlueManager_discover<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) {
        info!("BlueManager::discover()");

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

            let mut events = central.events().await.unwrap();
            let _ = central.start_scan(ScanFilter::default()).await;

            while let Some(event) = events.next().await {
                match event {
                    CentralEvent::DeviceDiscovered(id) => {
                        info!("DeviceDiscovered: {:?}", id);
                    }
                    CentralEvent::DeviceConnected(id) => {
                        info!("DeviceConnected: {:?}", id);
                    }
                    CentralEvent::DeviceDisconnected(id) => {
                        info!("DeviceDisconnected: {:?}", id);
                    }
                    CentralEvent::ManufacturerDataAdvertisement {
                        id,
                        manufacturer_data,
                    } => {
                        info!(
                            "ManufacturerDataAdvertisement: {:?}, {:?}",
                            id, manufacturer_data
                        );
                    }
                    CentralEvent::ServiceDataAdvertisement { id, service_data } => {
                        info!("ServiceDataAdvertisement: {:?}, {:?}", id, service_data);
                    }
                    CentralEvent::ServicesAdvertisement { id, services } => {
                        let services: Vec<String> =
                            services.into_iter().map(|s| s.to_short_string()).collect();
                        info!("ServicesAdvertisement: {:?}, {:?}", id, services);
                    }
                    _ => {}
                }
            }

            let _ = central.stop_scan().await;
        };

        let tokio_rt = Runtime::new().expect("Creating Tokio Runtime should not fail");
        let handle = tokio_rt.handle();
        let _adapters = handle.block_on(block);
    }
}

#[cfg(test)]
mod tests {
    // use super::*;

    #[test]
    fn it_works() {
        assert_eq!(true, true);
    }
}
