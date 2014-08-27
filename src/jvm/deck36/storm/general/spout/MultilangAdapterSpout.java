/**
* Storm spout to wrap a multilang spout implementation.
*
* @author Stefan Schadwinkel <stefan.schadwinkel@deck36.de>
* @copyright Copyright (c) 2013 DECK36 GmbH & Co. KG (http://www.deck36.de)
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*
*/

package deck36.storm.general.spout;

import backtype.storm.spout.ShellSpout;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import java.util.Map;


@SuppressWarnings("unchecked")
public class MultilangAdapterSpout extends ShellSpout implements IRichSpout {

    String[] _outputFields;

    public MultilangAdapterSpout(String executor, String component, String parameter, String... outputFields) {

        super(executor, component, parameter);

        _outputFields = outputFields;

    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(_outputFields));
    }


    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }


}
