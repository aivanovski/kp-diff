![https://github.com/aivanovski/kp-diff/workflows/Build/badge.svg](https://github.com/aivanovski/kp-diff/workflows/Build/badge.svg) ![Coverage](.github/badges/jacoco.svg)

# kp-diff
**kp-diff** is a CLI utility that compares and prints diff for Keepass database files.

## Demo
[![asciicast](https://asciinema.org/a/582798.svg)](https://asciinema.org/a/582798)

## Installation

### Linux

##### Arch Linux
`kp-diff` is available in the [Arch User Repository](https://aur.archlinux.org/) and could be installed with any [AUR](https://aur.archlinux.org/) helper.
```
yay -S kp-diff-bin
```

##### Manual installation
All releases of `kp-diff` can be downloaded from [releases](https://github.com/aivanovski/kp-diff/releases) page

##### Command line installation via `curl`
A particular version of `kp-diff` can be downloaded with next command which also changes the file to an executable in directory `$HOME/.local/bin`
```
curl -sSLO https://github.com/aivanovski/kp-diff/releases/download/0.5.0/kp-diff-linux-amd64 && chmod +x kp-diff-linux-amd64 && mkdir -p $HOME/.local/bin && mv kp-diff-linux-amd64 $HOME/.local/bin/kp-diff
```

### macOS
`kp-diff` is available as Homebrew formula (formulas [repositry](https://github.com/aivanovski/homebrew-brew))
```
brew tap aivanovski/brew
brew install kp-diff
```

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
    -d, --diff-by                    Type of differ, default is 'path'. Possible values:
                                          path - produces more accurate diff, considers entries identical if they have identical content but UUID differs
                                          uid - considers entries identical if they have identical content and UUID
    -v, --verbose                    Print verbose output (entry fields will be printed)
    -V, --version                    Print version
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
Binary file provides faster startup and better performance. It can be compiled (ahead-of-time) from `.jar` file with [GraalVM](https://www.graalvm.org/) with
the following steps:
- Install and setup latest version of [GraalVM](https://www.graalvm.org/) (for example version >= 21)
- Compile binary with following command:
```
native-image \
    --no-server \
    --no-fallback \
    -H:IncludeResources=".*\.properties" \
    --allow-incomplete-classpath \
    -jar PATH_TO_JAR_FILE
```
