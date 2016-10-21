package com.hurence.logisland.engine.spark

import java.text.SimpleDateFormat
import java.util
import java.util.{Collections, Date}

import com.hurence.logisland.component.PropertyDescriptor
import com.hurence.logisland.engine.EngineContext
import com.hurence.logisland.processor.StandardProcessContext
import com.hurence.logisland.processor.chain.{KafkaRecordStream, StandardProcessorChainInstance}
import com.hurence.logisland.record.{Field, FieldDictionary, FieldType, Record}
import com.hurence.logisland.util.kafka.KafkaSink
import com.hurence.logisland.util.spark.ZookeeperSink
import com.hurence.logisland.util.validator.StandardValidators
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SaveMode}
import org.apache.spark.streaming.kafka.HasOffsetRanges
import org.slf4j.LoggerFactory

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


object HdfsBurnerEngine {

    val OUTPUT_FOLDER_PATH = new PropertyDescriptor.Builder()
        .name("output.folder.path")
        .description("the location where to put files : file:///tmp/out")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build


    val OUTPUT_FORMAT = new PropertyDescriptor.Builder()
        .name("output.format")
        .description("can be parquet, orc csv")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues("parquet", "orc", "txt", "json")
        .build

    val RECORD_TYPE = new PropertyDescriptor.Builder()
        .name("record.type")
        .description("the type of event to filter")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build
}


class HdfsBurnerEngine extends AbstractSparkStreamProcessingEngine {




    override def getSupportedPropertyDescriptors: util.List[PropertyDescriptor] = {
        val descriptors: util.List[PropertyDescriptor] = new util.ArrayList[PropertyDescriptor]
        descriptors.add(SparkStreamProcessingEngine.SPARK_APP_NAME)
        descriptors.add(SparkStreamProcessingEngine.SPARK_MASTER)
        descriptors.add(SparkStreamProcessingEngine.SPARK_YARN_DEPLOYMODE)
        descriptors.add(SparkStreamProcessingEngine.SPARK_YARN_QUEUE)
        descriptors.add(SparkStreamProcessingEngine.SPARK_DRIVER_MEMORY)
        descriptors.add(SparkStreamProcessingEngine.SPARK_EXECUTOR_MEMORY)
        descriptors.add(SparkStreamProcessingEngine.SPARK_DRIVER_CORES)
        descriptors.add(SparkStreamProcessingEngine.SPARK_EXECUTOR_CORES)
        descriptors.add(SparkStreamProcessingEngine.SPARK_EXECUTOR_INSTANCES)
        descriptors.add(SparkStreamProcessingEngine.SPARK_SERIALIZER)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_BLOCK_INTERVAL)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_KAFKA_MAX_RATE_PER_PARTITION)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_BATCH_DURATION)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_BACKPRESSURE_ENABLED)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_UNPERSIST)
        descriptors.add(SparkStreamProcessingEngine.SPARK_UI_PORT)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_TIMEOUT)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_KAFKA_MAXRETRIES)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_UI_RETAINED_BATCHES)
        descriptors.add(SparkStreamProcessingEngine.SPARK_STREAMING_RECEIVER_WAL_ENABLE)
        descriptors.add(HdfsBurnerEngine.OUTPUT_FOLDER_PATH)
        descriptors.add(HdfsBurnerEngine.OUTPUT_FORMAT)
        descriptors.add(HdfsBurnerEngine.RECORD_TYPE)
        Collections.unmodifiableList(descriptors)
    }


    def process(rdd: RDD[(Array[Byte], Array[Byte])],
                engineContext: EngineContext,
                processorChainInstance: StandardProcessorChainInstance,
                kafkaSink: Broadcast[KafkaSink],
                zkSink: Broadcast[ZookeeperSink]): Unit = {

        if (!rdd.isEmpty()) {
            val processorChainContext = new StandardProcessContext(processorChainInstance)
            // Cast the rdd to an interface that lets us get an array of OffsetRange
            val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

            // Get the singleton instance of SQLContext
            val sqlContext = new org.apache.spark.sql.SQLContext(rdd.sparkContext)
            // this is used to implicitly convert an RDD to a DataFrame.

            val deserializer = getSerializer(
                processorChainContext.getProperty(KafkaRecordStream.INPUT_SERIALIZER).asString,
                processorChainContext.getProperty(KafkaRecordStream.AVRO_INPUT_SCHEMA).asString)




            def convertToRow(record: Record): Row = {
                Row.fromSeq(
                    record.getAllFieldsSorted.toArray(Array[Field]()).map(f => {
                        f.getType match {
                            case FieldType.INT => f.asInteger().intValue()
                            case FieldType.LONG => f.asLong().longValue()
                            case FieldType.FLOAT => f.asFloat().floatValue()
                            case FieldType.DOUBLE => f.asDouble().doubleValue()
                            case FieldType.STRING => f.asString()
                            case _ => f.asString()
                        }
                    }).toSeq
                )
            }

            def convertFieldsNameToSchema(record: Record): StructType = {
                StructType(
                    record.getAllFieldsSorted.toArray(Array[Field]()).map(f => {
                        f.getType match {
                            case FieldType.INT => StructField(f.getName, IntegerType, nullable = true)
                            case FieldType.LONG => StructField(f.getName, LongType, nullable = true)
                            case FieldType.FLOAT => StructField(f.getName, FloatType, nullable = true)
                            case FieldType.DOUBLE => StructField(f.getName, DoubleType, nullable = true)
                            case FieldType.STRING => StructField(f.getName, StringType, nullable = true)
                            case _ => StructField(f.getName, StringType, nullable = true)
                        }
                    })
                )
            }



            val sdf = new SimpleDateFormat("yyyy-MM-dd")
            val today = sdf.format(new Date())

            val recordType = engineContext.getProperty(HdfsBurnerEngine.RECORD_TYPE).asString()
            val outPath = engineContext.getProperty(HdfsBurnerEngine.OUTPUT_FOLDER_PATH).asString() +
                s"/day=$today" +
                s"/record_type=$recordType"


            val records = rdd.mapPartitions(p => deserializeEvents(p, deserializer).iterator)
                .filter(r =>
                    r.hasField(FieldDictionary.RECORD_TYPE) &&
                    r.getField(FieldDictionary.RECORD_TYPE).asString() == recordType
                )

            if(!records.isEmpty()){
                val rows = records.map(r => convertToRow(r))
                val schema = convertFieldsNameToSchema(records.take(1)(0))

                sqlContext.createDataFrame(rows, schema)
                    .write
                    //      .partitionBy(FieldDictionary.RECORD_TYPE)
                    .mode(SaveMode.Append)
                    // TODO choose output format
                    .parquet(outPath)
            }



            // TODO save offsets ranges
        }
    }

}