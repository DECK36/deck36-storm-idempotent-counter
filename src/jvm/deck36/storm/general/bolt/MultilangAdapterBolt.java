/**
* Storm bolt to wrap a multilang bolt implementation.
*
* @author Stefan Schadwinkel <stefan.schadwinkel@deck36.de>
* @copyright Copyright (c) 2013 DECK36 GmbH & Co. KG (http://www.deck36.de)
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*
*/

package deck36.storm.general.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.ShellBolt;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import java.util.List;
import java.util.Map;


@SuppressWarnings("unchecked")
public class MultilangAdapterBolt extends ShellBolt implements IRichBolt {

    String[] _outputFields;

    public MultilangAdapterBolt(String executor, String component, String parameter, String... outputFields) {

        super(executor, component, parameter);

        _outputFields = outputFields;

    }

    public MultilangAdapterBolt(String[] command, String... outputFields) {

        super(command);

        _outputFields = outputFields;
    }

    public MultilangAdapterBolt(List<String> command, String... outputFields) {

        super((String[]) command.toArray(new String[] {}));

        _outputFields = outputFields;
    }



    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        if (_outputFields != null) {
            declarer.declare(new Fields(_outputFields));
        }
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

}
