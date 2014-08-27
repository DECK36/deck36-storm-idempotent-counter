deck36-storm-idempotent-counter
===============================

Tutorial code to create an idempotent counter for use within Apache Storm in order to avoid over-counting during tuple re-play due to failures. 

The idempotency of the counter is created by using a stable, unique message id (provided by Apache Kafka) 
to identify unique instances during replay and using a HyperLogLog structure (provided by Redis) to count. 

Read more about the method on the DECK36 blog: [No more Over-Counting: Making Apache Storm Counters Easy and Idempotent using Kafka and Redis](https://blog.deck36.de/no-more-over-counting-making-counters-in-apache-storm-idempotent-using-redis-hyperloglog)

## Prerequisites 

You need:
- Redis running on localhost:6379
- Kafka running on localhost with a configured `test` topic. You can achieve that by following [the Kafka Quick Start instructions](http://kafka.apache.org/documentation.html#quickstart).
- Node.js and npm
- Storm 0.9.2 (Just unzip to have the `storm` command. No configuration necessary, because only `LocalCluster` is used for the tutorial.)

## Build

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


## Run

Currently, the topology is executed using `LocalCluster` only. We need to execute through the `storm jar` command in order to make the multilang dependencies (a node.js bolt) available within Storm. 

	storm jar target/deck36-idempotent-counter-0.0.1-SNAPSHOT-standalone.jar deck36.IdempotentCounterTopology


## Other code

- Uses code from [epokmedia/storm-node-multilang](https://github.com/epokmedia/storm-node-multilang) for the Node.js bolt. 
- Check [our storm fork](https://github.com/DECK36/incubator-storm/tree/storm-kafka-spout-with-offset) for the patched `KafkaSpout` and `KafkaOffsetWrapperScheme`.   


