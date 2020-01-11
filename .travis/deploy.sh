#!/usr/bin/env sh

./gradlew uploadArchives \
    -Psigning.keyId=${GPG_KEY_ID} \
    -Psigning.password=${GPG_PASSPHRASE} \
    -Psigning.secretKeyRingFile=${TRAVIS_BUILD_DIR}/pubring.gpg

