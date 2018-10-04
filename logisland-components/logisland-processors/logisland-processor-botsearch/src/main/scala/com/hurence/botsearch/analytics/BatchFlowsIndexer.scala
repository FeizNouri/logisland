/**
 * Copyright (C) 2016 Hurence (support@hurence.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hurence.botsearch.analytics

import java.text.SimpleDateFormat
import java.util.Date

import com.hurence.botsearch.trace.NetworkFlowEventMapper
import com.hurence.logisland.util.spark.SparkUtils
import com.typesafe.scalalogging.slf4j.LazyLogging
import kafka.serializer.StringDecoder
import org.apache.commons.cli.{GnuParser, Options}
import org.apache.spark.sql.SQLContext
//import org.apache.spark.streaming.kafka.{KafkaUtils, OffsetRange}

/**
  * A demo program that index a few items
  *
  * https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+SimpleConsumer+Example
  *
  *
  */
@deprecated("should use generic event indexer job", "0.9.4")
object BatchFlowsIndexer extends LazyLogging {


    def main(args: Array[String]) {


        //////////////////////////////////////////
        // Commande lien management
        val parser = new GnuParser()
        val options = new Options()
        options.addOption("o", "output", true, "es, solr, debug")
        options.addOption("w", "time-window", true, "window time for micro batch")
        options.addOption("b", "broker-list", true, "kafka broker list :localhost:9092,anotherhost:9092")
        options.addOption("t", "topic-list", true, "kafka topic list logisland1,logisland2")
        options.addOption("e", "es-config", true, "elasticsearch config : localhost")
        options.addOption("h", "help", false, "print usage")
        options.addOption("f", "folder-path", true, "parquet folder path")
        options.addOption("p", "parquet", false, "store to parquet ?")
        options.addOption("i", "index", false, "indexation ?")

        // parse the command line arguments
        val line = parser.parse(options, args)
        val output = line.getOptionValue("o", "debug")
        val windowTime = line.getOptionValue("w", "2").toLong
        val brokerList = line.getOptionValue("b", "sandbox:9092")
        val topicList = line.getOptionValue("t", "logisland")
        val esConfig = line.getOptionValue("e", "localhost")
        val doSaveAsParquet = line.hasOption("p")
        val doIndexation = line.hasOption("i")
        val source = "file://" + line.getOptionValue("f", "/usr/local/logisland/data/out")

        // set up context
        val sc = SparkUtils.initContext(this.getClass.getName)
        val sqlContext = new SQLContext(sc)
        import sqlContext.implicits._

        // Define the Kafka parameters, broker list must be specified
        val kafkaParams = Map("metadata.broker.list" -> brokerList, "group.id" -> "logisland-demo")

        // Define which topics to read from
        val topics = topicList.split(",").toSet

        // get first 100000 messages
        val lastOffest = KafkaOffsetUtils.getLastOffset(
            brokerList,
            "logisland",
            0,
            kafka.api.OffsetRequest.LatestTime)

    /*    val offsetRanges = Array(
            OffsetRange("logisland", 0, 0, lastOffest)
        )
        logger.info(s"last offset for kafka topic is $lastOffest")

        if (lastOffest != 0) {

            ////////////////////////////////////////
            // flows loading

            logger.info("Create the direct stream with the Kafka parameters and topics")
            val kafkaRdd = KafkaUtils.createRDD[String, String, StringDecoder, StringDecoder](
                sc, kafkaParams, offsetRanges)

            ////////////////////////////////////////
            // flows processing

            logger.info("convert raw lines to networkflow objects")
            val flows = kafkaRdd.map(kv => NetworkFlow.parse(kv._2))

            val dateSuffix = new SimpleDateFormat("yyyy.MM.dd").format(new Date())
            val esIndex = s"logisland-$dateSuffix"

            if (doIndexation) {
                logger.info("create es index")
                val mapper = new NetworkFlowEventMapper()
             //   val esIndexName = ElasticsearchUtils.createIndex(esHosts, "elasticsearch", "logisland", mapper)

                logger.info("launch flows indexation to es")
                //    EventIndexer.indexEvents(flows, esHosts, esIndexName, "flow")
            }

            if (doSaveAsParquet) {
                logger.info(s"save flows to parquet")
                flows.toDF().write.save(s"$source/$dateSuffix/flows.parquet")
            }

        }*/

        sc.stop()
    }
}
