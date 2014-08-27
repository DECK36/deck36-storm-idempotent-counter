;;
;;  Leiningen project file to build the Idempotent Counter Example
;;  Uses: Apache Storm, Apache Kafka, Redis
;;
;;  @author Stefan Schadwinkel <stefan.schadwinkel@deck36.de>
;;  @copyright Copyright (c) 2013 DECK36 GmbH & Co. KG (http://www.deck36.de)
;;
;;  For the full copyright and license information, please view the LICENSE
;;  file that was distributed with this source code.
;;


(defproject deck36-idempotent-counter "0.0.1-SNAPSHOT"
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm"]
  :aot :all
   
  :plugins [[lein-git-deps "0.0.1-SNAPSHOT"] [lein-idea "1.0.1"]]

  :resource-paths [
                        "resources/nodejs/"
                  ]

  :git-dependencies [
                        ["https://github.com/DECK36/incubator-storm.git" "storm-kafka-spout-with-offset"]
                    ]

  :dependencies [
                    [commons-collections/commons-collections "3.2.1"]
                    [com.googlecode.json-simple/json-simple "1.1"]
                    [redis.clients/jedis "2.5.2"]
                    [com.jayway.jsonpath/json-path "0.9.1"]
		            [com.google.code.gson/gson "2.2.4"]
		            [org.apache.kafka/kafka_2.9.2 "0.8.1.1"]
		            [org.apache.zookeeper/zookeeper "3.4.5"]
		            [org.apache.storm/storm-kafka "0.9.2-incubating"]
                 ]
              
  :exclusions [org.slf4j/slf4j-log4j12 log4j/log4j]

  :profiles {:provided
             {:dependencies
              [[org.apache.storm/storm-core "0.9.2-incubating"]]}}

  :min-lein-version "2.0.0"

)


