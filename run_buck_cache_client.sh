#!/usr/bin/env bash

# Remove ignite's default workspace
# Use this only when WAL is disabled
rm -rf ignite

./gradlew distJar

$JAVA_HOME/bin/java -Xmx16g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:MaxDirectMemorySize=6g -Dlog.home=/var/log/buck-cache-client/logs -jar cache/build/libs/cache-1.0.0-standalone.jar server cache/src/dist/config/$1.yml
