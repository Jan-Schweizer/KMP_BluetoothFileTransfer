use jni::{
    objects::{JObject, JValue},
    Executor,
};

use super::GLOBAL_JVM;

pub type Result<T> = core::result::Result<T, Error>;

#[derive(Debug)]
pub enum Error {
    Generic(String),
    DiscoveryNotPossible,
    AdapterNotAvailable,
}

impl From<bluer::Error> for Error {
    fn from(err: bluer::Error) -> Self {
        match err.kind {
            bluer::ErrorKind::DiscoveryActive => Self::DiscoveryNotPossible,
            _ => Self::Generic("An error occured".to_string()),
        }
    }
}
pub(crate) fn on_error(error: Error) {
    let blue_error_class_name = "de/schweizer/bft/BlueError";

    let exec = Executor::new(GLOBAL_JVM.get().unwrap().clone());
    let _ = exec.with_attached(|env| {
        let error_object = match error {
            Error::Generic(msg) => {
                let blue_error_class_name = format!("{}$Generic", blue_error_class_name);
                let blue_error_class = env.find_class(&blue_error_class_name).unwrap();
                let msg = env.new_string(msg).unwrap();
                env.new_object(
                    blue_error_class,
                    "(Ljava/lang/String;)V",
                    &[JValue::from(&msg)],
                )
                .unwrap()
            }
            Error::DiscoveryNotPossible => {
                let blue_error_class_name =
                    format!("{}$DiscoveryNotPossible", blue_error_class_name);
                let blue_error_class = env.find_class(&blue_error_class_name).unwrap();
                let blue_error_instance = env
                    .get_static_field(
                        blue_error_class,
                        "INSTANCE",
                        format!("L{};", &blue_error_class_name),
                    )
                    .unwrap();
                JObject::from(blue_error_instance.l().unwrap())
            }
            Error::AdapterNotAvailable => {
                let blue_error_class_name =
                    format!("{}$AdapterNotAvailable", blue_error_class_name);
                let blue_error_class = env.find_class(&blue_error_class_name).unwrap();
                let blue_error_instance = env
                    .get_static_field(
                        blue_error_class,
                        "INSTANCE",
                        format!("L{};", &blue_error_class_name),
                    )
                    .unwrap();
                JObject::from(blue_error_instance.l().unwrap())
            }
        };

        let blue_manager_cls = env
            .find_class("de/schweizer/bft/BlueManager")
            .expect("BlueManger could not be found");
        env.call_static_method(
            blue_manager_cls,
            "onError",
            "(Lde/schweizer/bft/BlueError;)V",
            &[JValue::from(&error_object)],
        )
        .unwrap()
        .v()
    });
}
