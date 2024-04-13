use bluer::Session;
use lazy_static::lazy_static;
use std::sync::{Arc, OnceLock};
use tokio::runtime::{Handle, Runtime};
use tokio::sync::Mutex;

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
    static ref BLUETOOTH_MANAGER: Mutex<BlueManager> = {
        let handle = rt_handle();
        let block = async {
            let session = Session::new()
                .await
                .expect("Creating bluer Session should not fail");
            let adapter = session.default_adapter().await.ok();
            let blue_manager = BlueManager {
                session: session,
                adapter: adapter,
            };
            blue_manager.set_discovery_filter().await;
            blue_manager
        };
        let blue_manager = handle.block_on(block);

        Mutex::new(blue_manager)
    };
}

fn bt_manager() -> &'static Mutex<BlueManager> {
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
