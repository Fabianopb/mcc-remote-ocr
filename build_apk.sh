#!/usr/bin/env bash

echo 'Starting to build the APK...'

cd TOCR-mobileUI/

./gradlew assembleRelease

echo 'APK saved in TOCR-mobileUI/app/build/outputs/apk/app-release-unsigned.apk'
