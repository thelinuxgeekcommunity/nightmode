#!/bin/bash
# A script to build Night Mode
# Licensed under the GPLv3
# Get the version for the AndroidManifest
VERSION=$(cat app/build.gradle | grep versionName | tr -d '"' | awk '{print $2}')
# Set the gradle arguments from the makerelease command line arguments
GRADLEARGS="$@"

# If the output directory doesn't exist, make it.
if [ ! -e apks ]; then
mkdir -p apks
fi

if [ ! -e apks/$VERSION ]; then
mkdir -p apks/$VERSION
fi

# Remove all apks from application/build/outputs/apk
rm -v app/build/outputs/apk/*.apk

# Build stuff here
echo "Building a debug build..."
./gradlew assembleNormalDebug $GRADLEARGS
echo "Copying the built apk to the output directory..."
cp -v app/build/outputs/apk/app-normal-debug.apk apks/$VERSION/NightMode-debug.apk

echo "Building a fdroid debug build..."
./gradlew assemblefdroidDebug $GRADLEARGS
echo "Copying the built apk to the output directory..."
cp -v app/build/outputs/apk/app-fdroid-debug.apk apks/$VERSION/NightMode-fdroid-debug.apk

if [ -e keystore.properties ]; then
echo "Building a release build..."
./gradlew assembleNormalRelease $GRADLEARGS
echo "Copying the built apk to the output directory..."
cp -v app/build/outputs/apk/app-normal-release.apk apks/$VERSION/NightMode-release.apk
else
echo "Keystore not found, not building a release build..."
fi

if [ -e keystore.properties ]; then
echo "Building a fdroid release build..."
./gradlew assemblefdroidRelease $GRADLEARGS
echo "Copying the built apk to the output directory..."
cp -v app/build/outputs/apk/app-fdroid-release.apk apks/$VERSION/NightMode-fdroid-release.apk
else
echo "Keystore not found, not building a fdroid release build..."
fi

