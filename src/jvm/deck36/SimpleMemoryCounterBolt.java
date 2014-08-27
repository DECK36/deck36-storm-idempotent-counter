/**
 * SimpleMemoryCounterVBolt
 *
 * Tokenizes an input string and counts each token occurrence in a
 * local hash map.
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
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SimpleMemoryCounterBolt extends BaseRichBolt {

    private double _tickFrequencyInSeconds;
    private Gson _gson;

    OutputCollector _collector;

    public SimpleMemoryCounterBolt(double tickFrequencyInSeconds) {
        _tickFrequencyInSeconds = tickFrequencyInSeconds;
    }

    private HashMap<String, Long> _counterMap;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _gson = new Gson();
        _counterMap = new HashMap<String, Long>();
        _collector = collector;
    }


    private void countTokens(Tuple tuple) {
        String[] tokens = tuple.getString(0).toLowerCase().replaceAll("[^\\p{Alpha} ]", "").split("\\s+");

        System.out.println("\n\nTUPLE FROM JAVA: " + tuple.toString() + " tokens: " + Arrays.toString(tokens) + "\n\n\n");

        for (String token : tokens) {

            Long value = _counterMap.get(token);

            if (value != null) {
                _counterMap.put(token, value + 1);
            } else {
                _counterMap.put(token, 1L);
            }
        }
    }

    private void emitCounterMap() {
        _collector.emit(new Values(_gson.toJson(_counterMap)));
    }

    private static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
                && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }

    @Override
    public void execute(Tuple tuple) {
        if (isTickTuple(tuple)) {
            emitCounterMap();
        } else {
            countTokens(tuple);
        }
        _collector.ack(tuple);
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("memory_counter_map"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {

        Config conf = new Config();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, _tickFrequencyInSeconds);

        return conf;
    }

}
