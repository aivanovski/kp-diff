#!/bin/bash

VERSION=$(grep 'val appVersion' build.gradle.kts | cut -d= -f2 | cut -d'"' -f2)

mkdir kp-diff_$VERSION
mkdir kp-diff_$VERSION/DEBIAN
mkdir kp-diff_$VERSION/usr
mkdir kp-diff_$VERSION/usr/local
mkdir kp-diff_$VERSION/usr/local/bin
cp ./kp-diff-linux-amd64 kp-diff_$VERSION/usr/local/bin/kp-diff
chmod +x kp-diff_$VERSION/usr/local/bin/kp-diff

PACKAGE_INFO=$(cat << EOF
Package: kp-diff
Version: $VERSION
Section: base
Priority: optional
Architecture: amd64
Depends: libc6 (>= 2.32)
Maintainer: Aliaksei Ivanouski <alexei.ivanovski@gmail.com>
Homepage: https://github.com/aivanovski/kp-diff
Website: https://github.com/aivanovski/kp-diff
Description: Utility to diff Keepass databases
EOF
)
echo "$PACKAGE_INFO" > kp-diff_$VERSION/DEBIAN/control

dpkg-deb --build kp-diff_$VERSION
