#[allow(non_snake_case)]
pub mod android {
    use android_logger::Config;
    use log::{info, LevelFilter};

    use jni;

    use self::jni::objects::{JClass, JString};
    use self::jni::sys::jstring;
    use self::jni::JNIEnv;

    #[no_mangle]
    pub extern "system" fn Java_de_schweizer_bft_BlueManager_initLogger<'local>(
        mut _env: JNIEnv<'local>,
        _class: JString<'local>,
    ) {
        android_logger::init_once(
            Config::default()
                .with_max_level(LevelFilter::Trace)
                .with_tag("Rust"),
        );
        info!("Android Logger initialized");
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
            .new_string(format!("Hello {} from Rust Android!", input))
            .expect("Couldn't create java string!");
        output.into_raw()
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