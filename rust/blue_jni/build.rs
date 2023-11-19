#![allow(dead_code)]
/// See https://github.com/android10/Rust-Cross-Platform-Development/blob/main/rust-library/cryptor_jni/build.rs for more info
use std::collections::HashMap;
use std::env;
use std::fs::File;
use std::io::Write;
use std::path::PathBuf;
use std::path::MAIN_SEPARATOR_STR;

use phf::phf_map;

use serde::Serialize;
use toml;
use util::{run_command, CommandConfig};

static ANDROID_NDK_VERSION: &str = "26.1.10909125";
static CARGO_CONFIG_DIR_NAME: &str = ".cargo";
static CARGO_CONFIG_FILE_NAME: &str = "config";

pub static ANDROID_TARGET_ABI_CONFIG: phf::Map<&'static str, (&'static str, &'static str)> = phf_map! {
    "armv7-linux-androideabi" => ("armv7a-linux-androideabi21-clang", "armeabi-v7a"),
    "aarch64-linux-android" => ("aarch64-linux-android21-clang", "arm64-v8a"),
    "i686-linux-android" => ("i686-linux-android21-clang", "x86"),
    "x86_64-linux-android" => ("x86_64-linux-android21-clang", "x86_64"),
};

// E.g /home/jan/Android/Sdk/ndk/26.1.10909125/toolchains/llvm/prebuilt/linux-x86_64/bin
fn toolchain_dir() -> String {
    let host_tag = match env::consts::OS {
        "linux" => "linux-x86_64",
        "macos" => "darwin-x86_64",
        "windows" => "windows-x86_64",
        _ => "linux-x86_64",
    };

    let toolchain_dir = PathBuf::new()
        .join(env::var("ANDROID_HOME").expect("ANDROID_HOME should be set!"))
        .join("ndk")
        .join(ANDROID_NDK_VERSION)
        .join("toolchains")
        .join("llvm")
        .join("prebuilt")
        .join(host_tag)
        .join("bin");

    toolchain_dir
        .as_os_str()
        .to_str()
        .expect("Creating a path should not fail")
        .to_string()
}

fn archiver_path() -> String {
    format!("{}{}llvm-ar", toolchain_dir(), MAIN_SEPARATOR_STR)
}

fn linker_path(linker_exe: &str) -> String {
    format!("{}{}{}", toolchain_dir(), MAIN_SEPARATOR_STR, linker_exe)
}

#[derive(Serialize)]
struct AndroidTargets<'a> {
    #[serde(rename(serialize = "target"))]
    targets: HashMap<&'a str, AndroidTargetConfig>,
}

#[derive(Serialize)]
struct AndroidTargetConfig {
    // https://doc.rust-lang.org/cargo/reference/config.html#targettriplear
    ar: String, // This option is deprecated and unused
    linker: String,
}

fn android_targets<'a>() -> AndroidTargets<'a> {
    let mut android_targets = AndroidTargets {
        targets: HashMap::with_capacity(ANDROID_TARGET_ABI_CONFIG.len()),
    };

    for (target, config) in ANDROID_TARGET_ABI_CONFIG.entries() {
        let target_config = AndroidTargetConfig {
            ar: archiver_path(),
            linker: linker_path(config.0),
        };

        android_targets.targets.insert(target, target_config);
    }

    android_targets
}

fn create_cargo_config_file() -> File {
    let current_dir = util::current_dir();
    let config_dir_path = current_dir.join(CARGO_CONFIG_DIR_NAME);
    util::create_dir(&config_dir_path);

    let config_file_path = config_dir_path.join(CARGO_CONFIG_FILE_NAME);
    util::create_file(&config_file_path)
}

fn create_android_targets_config_file() {
    let android_targets = android_targets();
    let toml = toml::to_string(&android_targets)
        .expect("Serializing android targets to toml shouldn't fail!");
    let mut config_file = create_cargo_config_file();
    match config_file.write_all(toml.as_bytes()) {
        Ok(_) => println!("Successfully wrote cargo configuration file."),
        Err(err) => panic!("Couldn't write cargo configuration file: {}", err),
    }
}

fn add_android_targets_to_toolchain() {
    let mut command_conf = CommandConfig {
        command: "rustup",
        args: vec!["target", "add"],
    };

    for target in ANDROID_TARGET_ABI_CONFIG.keys() {
        command_conf.args.push(target);
    }
    run_command(&command_conf);
}

fn main() {
    println!("cargo:rerun-if-changed=src/build.rs");

    create_android_targets_config_file();
    add_android_targets_to_toolchain();
}
