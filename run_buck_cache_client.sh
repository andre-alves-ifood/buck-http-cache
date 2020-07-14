#!/usr/bin/env bash

# Remove ignite's default workspace
# Use this only when WAL is disabled
rm -rf ignite

./gradlew distJar

$JAVA_HOME/bin/java \
    -Xmx8g \
    -Xms8g \
    -XX:+AlwaysPreTouch \
    -XX:+UseG1GC \
    -XX:+ScavengeBeforeFullGC \
    -XX:+DisableExplicitGC \
    -XX:MaxGCPauseMillis=200 \
    -XX:MaxDirectMemorySize=4g \
    -Djava.net.preferIPv4Stack=true \
    -Dlog.home=/var/log/buck-cache-client/logs \
    -jar cache/build/libs/cache-1.0.0-standalone.jar \
    server cache/src/dist/config/"$1".yml
