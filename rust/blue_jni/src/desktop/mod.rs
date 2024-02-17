use bluer::{DiscoveryFilter, Session};
use lazy_static::lazy_static;
use log::info;
use std::sync::{Arc, OnceLock};
use tokio::runtime::{Handle, Runtime};

use jni::objects::JClass;
use jni::{JNIEnv, JavaVM};

use crate::desktop::blue_manager::BlueManager;

mod blue_manager;
mod logger;

static GLOBAL_JVM: OnceLock<Arc<JavaVM>> = OnceLock::new();

lazy_static! {
    static ref TOKIO_RT: Runtime = Runtime::new().expect("Creating Tokio Runtime should not fail");
}

fn rt_handle() -> Handle {
    TOKIO_RT.handle().clone()
}

lazy_static! {
    static ref BLUETOOTH_MANAGER: BlueManager = {
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

        BlueManager {
            _session: session,
            adapter: adapter,
        }
    };
}

fn bt_manager() -> &'static BlueManager {
    &BLUETOOTH_MANAGER
}

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_ui_BftApp_init<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
) {
    let jvm = env
        .get_java_vm()
        .expect("Initializing the Global JVM should not fail");
    GLOBAL_JVM.set(Arc::new(jvm)).unwrap();
}
