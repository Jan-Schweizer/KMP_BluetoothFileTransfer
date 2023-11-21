#[path = "../../build.rs"]
mod build;

use std::path::Path;
use std::path::PathBuf;

static JNI_LIB_FILE_NAME: &str = "libblue_jni.so";

fn crate_lib_file(rust_target_dir: &Path, target: &str) -> PathBuf {
    rust_target_dir
        .join(target)
        .join("release")
        .join(JNI_LIB_FILE_NAME)
}

fn jni_libs_file(project_dir: &Path, kmp_target_dir: &str, kmp_main_dir: &str) -> PathBuf {
    let jni_libs_path = project_dir
        .join("composeApp")
        .join("src")
        .join(kmp_main_dir)
        .join("jniLibs")
        .join(kmp_target_dir);
    util::create_dir(&jni_libs_path);
    jni_libs_path.join(JNI_LIB_FILE_NAME)
}

fn publish_jni_libs_to_project() {
    let current_dir = util::current_dir();
    let project_dir = current_dir
        .parent()
        .expect("Current directory should have a parent!")
        .parent()
        .expect("Current directory should have a parent!");
    let rust_target_dir = project_dir.join("rust").join("target");

    for target in build::ANDROID_TARGET_ABI_CONFIG.keys() {
        let kmp_target_dir = build::ANDROID_TARGET_ABI_CONFIG
            .get(target)
            .expect(format!("Target: {} not available", target).as_str())
            .1;
        let crate_lib_file = crate_lib_file(&rust_target_dir, target);
        let jni_libs_file = jni_libs_file(project_dir, kmp_target_dir, "androidMain");

        util::copy_file(&crate_lib_file, &jni_libs_file)
    }

    for target in build::DESKTOP_TARGET_ABI_CONIG.keys() {
        let kmp_target_dir = build::DESKTOP_TARGET_ABI_CONIG
            .get(target)
            .expect(format!("Target: {} not available", target).as_str());
        let crate_lib_file = crate_lib_file(&rust_target_dir, target);
        let jni_libs_file = jni_libs_file(project_dir, kmp_target_dir, "desktopMain");

        util::copy_file(&crate_lib_file, &jni_libs_file)
    }
}

fn main() {
    publish_jni_libs_to_project();
}
