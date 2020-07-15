#!/usr/bin/env bash

./run_buck_cache_client.sh "$1" || echo "Restarting..." && sleep 5 && exec "$0" "$1"