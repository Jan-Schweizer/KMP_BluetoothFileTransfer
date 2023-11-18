use util::CommandConfig;

#[path = "../../build.rs"]
mod build;

fn release_android_targets() {
    for target in build::ANDROID_TARGET_ABI_CONFIG.keys() {
        println!("Building target: {}", target);
        let command = CommandConfig {
            command: "cargo",
            args: vec!["build", "--target", target, "--release"],
        };
        util::run_command(&command);
    }
}

fn main() {
    println!("Releasing Targets ...");
    release_android_targets();
}
