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
/**
  * Copyright (C) 2016 Hurence (bailet.thomas@gmail.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.hurence.logisland.util.kafka


import java.io.ByteArrayOutputStream

import com.hurence.logisland.record.Record
import com.hurence.logisland.serializer.RecordSerializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.JavaConversions._


/**
  * Lazy instanciate a Kafka producer that will be broadcasted
  * by the Spark driver into the Spark workers
  *
  * please note the partionning strategy
  * http://blog.rocana.com/kafkas-defaultpartitioner-and-byte-arrays
  *
  * @param createProducer
  */
class KafkaSink(createProducer: () => (KafkaProducer[Array[Byte], Array[Byte]], String)) extends Serializable {

    lazy val (producer, keyField) = createProducer()


    def shutdown() ={
        producer.close()
    }

    def send(topic: String, key: Array[Byte], value: Array[Byte]): Unit =
        producer.send(new ProducerRecord(topic, value))

    /**
      * Send events to Kafka topics
      *
      * @param events
      */
    def produce(topic: String, events: List[Record], serializer: RecordSerializer) = {

        events.foreach(event => {
            // messages are serialized with kryo first
            val baos: ByteArrayOutputStream = new ByteArrayOutputStream
            serializer.serialize(baos, event)

            // and then converted to KeyedMessage
            val key = if (event.hasField(keyField))
                event.getField(keyField).asString().getBytes()
            else
                null

            val message = new ProducerRecord(topic, key, baos.toByteArray)
            baos.close()


            producer.send(message)
        })
    }
}

object KafkaSink {
    def apply(config: Map[String, Object], keyField: String): KafkaSink = {
        val f = () => {
            val producer = new KafkaProducer[Array[Byte], Array[Byte]](config)

            sys.addShutdownHook {
                producer.close()
            }

            (producer, keyField)
        }
        new KafkaSink(f)
    }
}