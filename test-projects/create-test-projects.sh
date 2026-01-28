#!/bin/bash

# AGP versions to create test projects for
AGP_VERSIONS=("8.3.0" "8.4.0" "8.5.0" "8.6.0" "8.7.0" "8.8.0" "8.9.0" "8.10.0" "8.12.0" "9.0.0")

# Base directory
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

for VERSION in "${AGP_VERSIONS[@]}"; do
    # Create directory name (replace . with _)
    DIR_NAME="agp-${VERSION//./_}"
    PROJECT_DIR="$BASE_DIR/$DIR_NAME"

    echo "Creating test project for AGP $VERSION in $PROJECT_DIR"

    # Create directories
    mkdir -p "$PROJECT_DIR/app/src/main/java/com/didiglobal/booster/test"
    mkdir -p "$PROJECT_DIR/app/src/main/res/values"
    mkdir -p "$PROJECT_DIR/app/src/main/res/layout"
    mkdir -p "$PROJECT_DIR/buildSrc/src/main/groovy"

    echo "Created directories for $VERSION"
done

echo "All test project directories created!"
