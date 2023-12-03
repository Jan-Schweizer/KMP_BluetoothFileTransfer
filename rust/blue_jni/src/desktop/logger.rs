use env_logger::Env;
use log::info;

use self::jni::objects::JClass;
use self::jni::JNIEnv;
use jni;
use jni::objects::JString;

#[no_mangle]
pub extern "system" fn Java_de_schweizer_bft_Logger_init<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    log_level: JString<'local>,
) {
    let log_level: String = env
        .get_string(&log_level)
        .expect("Getting String from env should not fail")
        .into();
    let log_level = log_level.to_lowercase();
    env_logger::Builder::from_env(Env::default().default_filter_or(&log_level)).init();
    info!("Logger::init({})", &log_level);
}
