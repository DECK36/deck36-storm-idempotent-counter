#!/bin/bash

# install node.js redis client, must be in resources dir 
# in order to be packaged in the topology uberjar
cd resources/nodejs/resources/
npm install redis
cd ../../..

# get project dependencies 
lein deps

# get our patched KafkaSpout from github and build it
lein git-deps
cd .lein-git-deps/incubator-storm/external/storm-kafka/

mvn install 

cd ../../../..

# compile project code and create runnable topology uberjar 
lein uberjar



