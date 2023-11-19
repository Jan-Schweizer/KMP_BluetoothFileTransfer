use std::env;
use std::fs::File;
use std::io::{ErrorKind, Write};
use std::path::{Path, PathBuf};
use std::process::Command;
use std::{fs, io};

pub fn current_dir() -> PathBuf {
    match env::current_dir() {
        Ok(current_dir) => current_dir,
        Err(err) => {
            panic!("Could get current directory: {:?}", err);
        }
    }
}

pub fn create_dir(path: &Path) {
    fs::create_dir_all(&path).unwrap_or_else(|error| match error.kind() {
        ErrorKind::AlreadyExists => {}
        _ => panic!("Could not create directory: {}", error),
    });
}

pub fn create_file(file: &Path) -> File {
    File::create(&file).expect("Could not create file")
}

pub struct CommandConfig<'a> {
    pub command: &'a str,
    pub args: Vec<&'a str>,
}

pub fn run_command(command: &CommandConfig) {
    let mut command_with_args = Command::new(command.command);
    for arg in &command.args {
        command_with_args.arg(arg);
    }

    let output = command_with_args
        .output()
        .expect("Failed to execute command");
    io::stdout().write_all(&output.stdout).unwrap();
    io::stderr().write_all(&output.stderr).unwrap();
}

#[cfg(test)]
mod tests {
    // use super::*;

    #[test]
    fn it_works() {
        assert_eq!(true, true);
    }
}
