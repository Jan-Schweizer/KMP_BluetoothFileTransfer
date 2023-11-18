#[cfg(target_os = "android")]
#[allow(non_snake_case)]
pub mod android {
    extern crate jni;
    use self::jni::objects::{JClass, JString};
    use self::jni::sys::jstring;
    use self::jni::JNIEnv;

    #[no_mangle]
    pub extern "system" fn Java_de_schweizer_bft_BlueManager_discover<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        input: JString<'local>,
    ) -> jstring {
        println!("Where is this printed to?");
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
