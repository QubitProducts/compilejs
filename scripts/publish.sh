#!/bin/bash
current=$(dirname ${0})
echo $current
cd $current/..
. scripts/common.sh

echo "Compiling."
gradle jar

echo "Copying files."

mkdir current_release/

cp -v build/libs/*.jar current_release/
cp -v build/libs/compilejs.jar ../opentag-libraries/
cp -v build/libs/compilejs.jar ../opentag/nbproject/lib/compilejs.jar
cp -v build/libs/compilejs.jar ../tagsdk/nbproject/lib/compilejs.jar
cp -v build/libs/compilejs.jar ../tagsdk-build-tool/lib/compilejs.jar

echo "Finished."
