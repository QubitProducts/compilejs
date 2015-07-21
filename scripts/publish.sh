#!/bin/bash
current=$(dirname ${0})
echo $current
cd $current/..
. scripts/common.sh

echo "Compiling."
ant jar

echo "Copying files."

cp -v dist/*.jar current_release/
cp -v dist/compilejs.jar ../opentag-libraries/
cp -v dist/compilejs.jar ../opentag/nbproject/lib/compilejs.jar
cp -v dist/compilejs.jar ../tagsdk/nbproject/lib/compilejs.jar
cp -v dist/compilejs.jar ../tagsdk-build-tool/lib/compilejs.jar

echo "Finished."
