#!/bin/bash

# Gradle wrapper for Android project

# Determine the directory of the script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Find the gradle wrapper jar
GRADLE_WRAPPER_JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "Error: Gradle wrapper jar not found at $GRADLE_WRAPPER_JAR"
    echo "Please ensure you have the complete Gradle wrapper setup."
    exit 1
fi

# Determine Java command
if [ -n "${JAVA_HOME}" ]; then
    JAVA_CMD="${JAVA_HOME}/bin/java"
else
    JAVA_CMD="java"
fi

# Execute Gradle wrapper
exec "$JAVA_CMD" -jar "$GRADLE_WRAPPER_JAR" "$@"
