#!/usr/bin/env bash

echo "Waiting for Elasticsearch to be available..."
while ! curl -s localhost:9200 &> /dev/null; do
  sleep 1
done
echo "Elasticsearch is up and ready!"
