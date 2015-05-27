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

echo "Finished."