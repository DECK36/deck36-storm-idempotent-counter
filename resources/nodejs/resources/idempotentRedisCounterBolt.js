/**
* Storm bolt to update Redis HyperLogLogs for each token of a received message,
* taking a unique message id into to account. If the id is stable across repetitions,
* this creates an idempotent token counter.
*
* @author Stefan Schadwinkel <stefan.schadwinkel@deck36.de>
* @copyright Copyright (c) 2013 DECK36 GmbH & Co. KG (http://www.deck36.de)
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*
*/

var Bolt = require('./bolt').Bolt;
var nodeRedis = require('./node_modules/redis');

//The Bolt constructor takes a definition function,
//an input stream and an output stream
var joinBolt = new Bolt(function(events) {
    var collector = null;
    var redis = null;

    //You can listen to the bolt "prepare" event to
    //fetch a reference to the OutputCollector instance
    events.on('prepare', function(c) {
        collector = c;
        redis = nodeRedis.createClient();
    });

    //The definition function must return a function used as
    //the execute function.
    return function(tuple, cb) {

        var redisSetKey = "nodejs:deck36:idempotent:counters"

        this._protocol.sendLog("\n\n\nTUPLE FROM NODE.JS: " + JSON.stringify(tuple) + "\n\n");

        var object = tuple["values"];
        var key  = object[0].toLowerCase().replace(/\W+/g," ").match(/\S+/g);
        var hash_offset = object[1];

        var redisKeySet = new Array();
        for (var i = 0; i < key.length; i++) {
            var hash = hash_offset + "_" + i

            var redisKey = "nodejs:deck36:idempotent:counter:" + key[i];
            redisKeySet.push(redisKey);

            redis.sadd(redisSetKey, key[i]);
            redis.pfadd(redisKey, hash);
        }

        collector.ack(tuple);
        cb();
    }
}, process.stdin, process.stdout);

process.stdin.setEncoding('utf8');
process.stdin.resume();

