/**
 * EmitCurrentIdempotentRedisCountsBolt
 *
 * Periodically queries a Redis store for a set of keys that identify
 * a group of HyperLogLogs and queries each one to emit a map of all
 * current values of each hyperLogLog.
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
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class EmitCurrentIdempotentRedisCountsBolt extends BaseRichBolt {

    OutputCollector _collector;

    private double _tickFrequencyInSeconds;

    private String _redisCounterKeyPrefix;
    private String _redisKeySet;
    private JedisPool _pool;
    private Gson _gson;

    public EmitCurrentIdempotentRedisCountsBolt(String redisKeySet, String redisCounterKeyPrefix, double tickFrequencyInSeconds) {
        _redisKeySet = redisKeySet;
        _redisCounterKeyPrefix = redisCounterKeyPrefix;
        _tickFrequencyInSeconds = tickFrequencyInSeconds;
    }


    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _pool = new JedisPool("localhost", 6379);
        _gson = new Gson();
        _collector = collector;
    }


    @Override
    public void execute(Tuple tuple) {

        Jedis jedis = _pool.getResource();
        Map<String, Long> currentCounters = new HashMap<>();

        try {

            Set<String> redisCounterKeys = jedis.smembers(_redisKeySet);

            for (String redisKey : redisCounterKeys) {
                Long thisCount = jedis.pfcount(_redisCounterKeyPrefix + redisKey);
                currentCounters.put(redisKey, thisCount);
            }

            _collector.emit(new Values(_gson.toJson(currentCounters)));
            _collector.ack(tuple);

        } catch (JedisConnectionException e) {
            // returnBrokenResource when the state of the object is unrecoverable
            _pool.returnBrokenResource(jedis);
            _collector.fail(tuple);
        } finally {
            /// ... it's important to return the Jedis instance to the _pool once you've finished using it
            _pool.returnResource(jedis);
        }

    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("redis_counter_map"));
    }

    @Override
    public void cleanup() {
        _pool.destroy();
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, _tickFrequencyInSeconds);
        return conf;
    }

}
