/**
 * IdempotentCounterTopology
 *
 * Uses Apache Kafka (for unique message ids) and Redis HyperLogLog to provide
 * idempotent counters for use with Apache Storm. Using idempotent counters
 * prevents over-counting during failure-induced tuple re-play.
 *
 *
 * @author Stefan Schadwinkel <stefan.schadwinkel@deck36.de>
 * @copyright Copyright (c) 2013 DECK36 GmbH & Co. KG (http://www.deck36.de)
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 *
 */

package deck36;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import deck36.storm.general.bolt.MultilangAdapterBolt;
import deck36.storm.general.bolt.PrinterBolt;
import deck36.storm.general.bolt.RandomFailureBolt;
import storm.kafka.*;


public class IdempotentCounterTopology
{

    public static void main(String[] args) throws Exception
    {
        // Get the topology builder
        TopologyBuilder builder = new TopologyBuilder();

        // For demo purposes, we don't use any parallelism
        int parallelismHint = 1;
        int numTasks = 1;

        // Configure KafkaSpout
        SpoutConfig spoutConfig = new SpoutConfig(
                new ZkHosts("localhost"),
                "test", 									// topic
                "/kafka-spout-idempotent-counter-demo", 	// zkRoot
                "kafka-spout-idempotent-counter-demo-id" 	// zkSpoutId
        );

        // Because our patched KafkaSpout appends the Kafka offset to each tuple,
        // we need to use our Scheme wrapper to define a field name for the offset
        spoutConfig.scheme = new SchemeAsMultiScheme(new KafkaOffsetWrapperScheme(new StringScheme()));

        // For demo purpose, we always consume the whole topic from the start
        spoutConfig.forceFromStart = true;

        // Create our KafkaSpout
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

        // Add spout to topology
        builder.setSpout("kafka-spout", kafkaSpout, parallelismHint)
                .setNumTasks(numTasks);

        // Add the RandomFailureBolt:
        // It will randomly fail the given proportion of the tuples it receives.
        // Here, we will fail 90% of the messages to force Storm to replay tuples very often.
        builder.setBolt("random_failure", new RandomFailureBolt(0.9), parallelismHint)
                .setNumTasks(numTasks)
                .shuffleGrouping("kafka-spout");

        // Add PrinterBolt to log the KafkaSpout
        builder.setBolt("print_kafka_spout", new PrinterBolt(), parallelismHint)
                .setNumTasks(numTasks)
                .shuffleGrouping("kafka-spout");

        // Add idempotent counter:
        // This bolt implements the actual interaction with Redis.
        // We use the MultilangAdapterBolt wrapper to easily use the node.js bolt.
        builder.setBolt("idempotent_counter", new MultilangAdapterBolt(
                new String[] {
                    "node",
                    "idempotentRedisCounterBolt.js"
                }, "result"),
                parallelismHint)
                .setNumTasks(numTasks)
                .shuffleGrouping("kafka-spout");

        // Add the SimpleMemoryCounterBolt:
        // This bolt uses tick tuples to periodically emit its counter map as a JSON string
        // Here, we emit the counts every 5 seconds.
        builder.setBolt("overcounting_memory_counter", new SimpleMemoryCounterBolt(5), parallelismHint)
                .setNumTasks(numTasks)
                .shuffleGrouping("kafka-spout");

        // Add PrinterBolt to log the SimpleMemoryCounterBolt
        builder.setBolt("print_memory_count", new PrinterBolt(), parallelismHint)
                .setNumTasks(numTasks)
                .shuffleGrouping("overcounting_memory_counter");

        // Add the EmitCurrentIdempotentRedisCountsBolt:
        // This bolt the uses tick tuples to periodically query redis to fetch all counts.
        // It emits the counter map as a JSON string.
        // Here, we fetch the counts every 5 seconds as well.
        builder.setBolt("emit_redis_counts",
                new EmitCurrentIdempotentRedisCountsBolt(
                        "nodejs:deck36:idempotent:counters",
                        "nodejs:deck36:idempotent:counter:", 5))
                .setNumTasks(numTasks);

        // Add PrinterBolt to log the EmitCurrentIdempotentRedisCountsBolt
        builder.setBolt("print_redis_counts", new PrinterBolt(), parallelismHint)
                .setNumTasks(numTasks)
                .shuffleGrouping("emit_redis_counts");



        // Submit topology
        Config conf = new Config();

        // Default is to use LocalCluster for Demo purposes
        String submitStyle = "local";

        if (submitStyle.equals("submit")) {

            // Number of Workers (JVM processes) to use for running the whole topology across the whole cluster.
            // Usually, one machine in the cluster has 4 Worker JVMs.
            conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());

        } else if (submitStyle.equals("local")) {

            conf.setNumWorkers(3);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(60000000);
            cluster.killTopology("test");
            cluster.shutdown();

        }

    }

}
