/**
 * RandomFailureBolt
 *
 * Fails incoming tuples at random with a pre-determined probability.
 *
 * @author Stefan Schadwinkel <stefan.schadwinkel@deck36.de>
 * @copyright Copyright (c) 2013 DECK36 GmbH & Co. KG (http://www.deck36.de)
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 *
 */


package deck36.storm.general.bolt;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Tuple;

import java.util.Map;
import java.util.Random;

public class RandomFailureBolt extends BaseRichBolt {

    private double _ratio = 0.1;
    private Random _random;

    OutputCollector _collector;

    public RandomFailureBolt() {}

    public RandomFailureBolt(Double ratio) {
        _ratio = ratio;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _random = new Random();
        _collector = collector;
    }


    @Override
    public void execute(Tuple tuple) {

        if (_random.nextDouble() < _ratio) {
            _collector.fail(tuple);
        } else {
            _collector.ack(tuple);
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

}
