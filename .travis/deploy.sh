#!/usr/bin/env sh

./gradlew publishToSonatype \
    -POSSRH_USERNAME=${OSSRH_USERNAME} \
    -POSSRH_PASSWORD=${OSSRH_PASSWORD} \
    -Psigning.keyId=${GPG_KEY_ID} \
    -Psigning.password=${GPG_PASSPHRASE} \
    -Psigning.secretKeyRingFile=${TRAVIS_BUILD_DIR}/.travis/secring.gpg

