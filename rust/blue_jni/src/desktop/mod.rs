use bluer::{DiscoveryFilter, Session};
use lazy_static::lazy_static;
use log::info;
use std::collections::HashMap;
use std::sync::{Mutex, MutexGuard};
use tokio::runtime::{Handle, Runtime};

use crate::desktop::blue_manager::BlueManager;

mod blue_manager;
mod logger;

lazy_static! {
    static ref TOKIO_RT: Runtime = Runtime::new().expect("Creating Tokio Runtime should not fail");
}

fn rt_handle() -> Handle {
    TOKIO_RT.handle().clone()
}

lazy_static! {
    static ref BLUETOOTH_MANAGER: Mutex<BlueManager> = {
        let handle = rt_handle();
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

            (session, adapter)
        };
        let (session, adapter) = handle.block_on(block);

        Mutex::new(BlueManager {
            _session: session,
            adapter: adapter,
            device_addrs: HashMap::new(),
        })
    };
}

fn bt_manager() -> MutexGuard<'static, BlueManager> {
    BLUETOOTH_MANAGER.lock().unwrap()
}
