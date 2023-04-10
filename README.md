# kp-diff
**kp-diff** is a CLI utility for comparing Keepass database files.


## Installation
#### Binary file installation (for Linux only)
- Download `kp-diff-linux` binary from [Release page](https://github.com/aivanovski/kp-diff/releases)

#### Jar file installation (for Linux or Mac OS)
- Install Java version >= 11
- Download `kp-diff.jar` from [Release page](https://github.com/aivanovski/kp-diff/releases)

## How to run
#### For binary file
`kp-diff file1.kdbx file2.kdbx`
#### For `.jar` file
`java -jar kp-diff.jar file1.kdbx file2.kdbx`

## Usage
```
USAGE:
    kp-diff [OPTIONS] <FILE-A> <FILE-B>

ARGS:
    <FILE-A>    First file
    <FILE-B>    Second file

OPTIONS:
    -o, --one-password               Use one password for both files
    -k, --key-file                   Path to key file for <FILE-A> and <FILE-B>
    -a, --key-file-a                 Path to key file for <FILE-A>
    -b, --key-file-b                 Path to key file for <FILE-B>
    -v, --version                    Print version
    -h, --help                       Print help information
```

## Building from sources
#### Building `.jar` file
```
git clone https://github.com/aivanovski/kp-diff.git
cd kp-diff
```
Then to build the project run:
```
./gradlew shadowJar
```
Output file should be localed at `kp-diff/build/libs/`
#### Building binary file
`kp-diff.jar` file can be compiled to the executable binary with GraalVM
