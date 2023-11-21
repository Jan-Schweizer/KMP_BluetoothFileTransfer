#[allow(non_snake_case)]
mod desktop {
    use env_logger::Env;
    use log::info;

    use jni;

    use self::jni::objects::{JClass, JString};
    use self::jni::sys::jstring;
    use self::jni::JNIEnv;

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
        input: JString<'local>,
    ) -> jstring {
        info!("BlueManager::discover()");
        let input: String = env
            .get_string(&input)
            .expect("Couldn't get java string!")
            .into();
        let output = env
            .new_string(format!("Hello {} from Rust Desktop!", input))
            .expect("Couldn't create java string!");
        output.into_raw()
    }
}
