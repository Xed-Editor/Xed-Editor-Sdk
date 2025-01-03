#!/bin/bash

# Set the root directory of your Android project
PROJECT_ROOT=/home/rohit/AndroidStudioProjects/Xed-Editor
CORE_DIR="$PROJECT_ROOT/core"
OUTPUT_DIR="./libs"

# Ensure the output directory exists
mkdir -p "$OUTPUT_DIR"

# Find all subdirectories within the core directory
MODULES=$(find "$CORE_DIR" -mindepth 1 -maxdepth 1 -type d)

# Iterate over each module and copy the full.jar file
for MODULE in $MODULES; do
    FULL_JAR_PATH="$MODULE/build/intermediates/full_jar/release/createFullJarRelease/full.jar"

    # Check if the full.jar file exists
    if [[ -f "$FULL_JAR_PATH" ]]; then
        # Extract the module name to name the output file
        MODULE_NAME=$(basename "$MODULE")
        OUTPUT_JAR="$OUTPUT_DIR/$MODULE_NAME.jar"

        # Copy the jar file to the lib directory
        cp "$FULL_JAR_PATH" "$OUTPUT_JAR"
        echo "Copied $FULL_JAR_PATH to $OUTPUT_JAR"
    else
        echo "Warning: full.jar not found for module $MODULE"
    fi
done

echo "All jars have been copied to $OUTPUT_DIR."
