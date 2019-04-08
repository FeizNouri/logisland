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
package com.hurence.logisland.engine.vanilla;

import com.hurence.logisland.component.ComponentFactory;
import com.hurence.logisland.config.EngineConfiguration;
import com.hurence.logisland.config.ProcessorConfiguration;
import com.hurence.logisland.config.StreamConfiguration;
import com.hurence.logisland.engine.EngineContext;
import com.hurence.logisland.engine.vanilla.stream.kafka.KafkaStreamsPipelineStream;
import com.hurence.logisland.engine.vanilla.stream.kafka.KafkaStreamProperties;
import com.hurence.logisland.record.FieldDictionary;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.serializer.RecordSerializer;
import com.hurence.logisland.serializer.SerializerProvider;
import com.hurence.logisland.util.runner.MockProcessor;
import com.salesforce.kafka.test.ProducedKafkaRecord;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.Supplier;

public class PlainJavaEngineTest {


    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    private static final String TOPIC_INPUT_1 = "topic.in";
    private static final String TOPIC_OUTPUT_1 = "topic.out1";
    private static final String TOPIC_OUTPUT_2 = "topic.out2";


    private EngineConfiguration engineConfiguration() {
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setType("engine");
        engineConfiguration.setDocumentation("Plain java engine");
        engineConfiguration.setComponent(PlainJavaEngine.class.getCanonicalName());
        return engineConfiguration;
    }

    private ProcessorConfiguration processorConfiguration(Map<String, String> props) {
        ProcessorConfiguration ret = new ProcessorConfiguration();
        ret.setProcessor(UUID.randomUUID().toString());
        ret.setComponent(MockProcessor.class.getCanonicalName());
        ret.setType("processor");
        return ret;
    }

    private final Supplier<Map<String, String>> defaultPropertySupplier(Map<String, String> props) {
        return () -> {
            Map<String, String> conf = new HashMap<>(props);
            conf.put("application.id", "test");
            conf.put("bootstrap.servers", sharedKafkaTestResource.getKafkaConnectString());
            conf.put(KafkaStreamProperties.READ_TOPICS.getName(), TOPIC_INPUT_1);
            conf.put(KafkaStreamProperties.WRITE_TOPICS.getName(), TOPIC_OUTPUT_1 + "," + TOPIC_OUTPUT_2);
            return conf;
        };
    }

    private final Supplier<Map<String, String>> defaultPropertySupplier() {
        return defaultPropertySupplier(Collections.emptyMap());
    }

    private StreamConfiguration emptyKafkaStream(Supplier<Map<String, String>> propertySupplier) {
        StreamConfiguration streamConfiguration = new StreamConfiguration();
        streamConfiguration.setStream("kafka_empty");
        streamConfiguration.setComponent(KafkaStreamsPipelineStream.class.getCanonicalName());
        streamConfiguration.setType("stream");
        streamConfiguration.setConfiguration(propertySupplier.get());
        return streamConfiguration;
    }

    @Test
    public void testEmpty() {
        EngineContext engineContext = ComponentFactory.getEngineContext(engineConfiguration()).get();
        Assert.assertTrue(engineContext.isValid());
        engineContext.getEngine().start(engineContext);
        engineContext.getEngine().awaitTermination(engineContext);
        engineContext.getEngine().shutdown(engineContext);
    }

    @Test
    public void testWithDefaultKafkaStream() {
        EngineConfiguration engineConfiguration = engineConfiguration();
        engineConfiguration.addPipelineConfigurations(emptyKafkaStream(defaultPropertySupplier()));
        EngineContext engineContext = ComponentFactory.getEngineContext(engineConfiguration).get();
        Assert.assertTrue(engineContext.isValid());
        engineContext.getEngine().start(engineContext);
        engineContext.getEngine().shutdown(engineContext);
        engineContext.getEngine().awaitTermination(engineContext);
    }

    @Test
    public void testWithBadConfiguredKafkaStream() {
        EngineConfiguration engineConfiguration = engineConfiguration();
        engineConfiguration.addPipelineConfigurations(emptyKafkaStream(defaultPropertySupplier(Collections.singletonMap("i.do.not.exist", "!"))));
        EngineContext engineContext = ComponentFactory.getEngineContext(engineConfiguration).get();
        Assert.assertFalse(engineContext.isValid());
    }


    @Test
    public void testCompletePipeline() {
        EngineConfiguration engineConfiguration = engineConfiguration();
        Map<String, String> props = new HashMap<>();
        props.put(KafkaStreamProperties.READ_TOPICS_SERIALIZER.getName(), KafkaStreamProperties.STRING_SERIALIZER.getValue());
        props.put(KafkaStreamProperties.WRITE_TOPICS_SERIALIZER.getName(), KafkaStreamProperties.EXTENDED_JSON_SERIALIZER.getValue());
        StreamConfiguration streamConfiguration = emptyKafkaStream(defaultPropertySupplier(props));
        streamConfiguration.addProcessorConfiguration(processorConfiguration(Collections.emptyMap()));
        engineConfiguration.addPipelineConfigurations(streamConfiguration);
        EngineContext engineContext = ComponentFactory.getEngineContext(engineConfiguration).get();
        Assert.assertTrue(engineContext.isValid());
        engineContext.getEngine().start(engineContext);
        sharedKafkaTestResource.getKafkaTestUtils().createTopic(TOPIC_INPUT_1, 1, (short) 1);
        sharedKafkaTestResource.getKafkaTestUtils().createTopic(TOPIC_OUTPUT_1, 1, (short) 1);
        sharedKafkaTestResource.getKafkaTestUtils().createTopic(TOPIC_OUTPUT_2, 1, (short) 1);

        List<ProducedKafkaRecord<byte[], byte[]>> inRecords = sharedKafkaTestResource.getKafkaTestUtils().produceRecords(100, TOPIC_INPUT_1, 0);
        List<ConsumerRecord<byte[], byte[]>> outRecords = sharedKafkaTestResource.getKafkaTestUtils().consumeAllRecordsFromTopic(TOPIC_OUTPUT_1);
        List<ConsumerRecord<byte[], byte[]>> outRecords2 = sharedKafkaTestResource.getKafkaTestUtils().consumeAllRecordsFromTopic(TOPIC_OUTPUT_2);
        Assert.assertEquals(inRecords.size(), outRecords.size());
        Assert.assertEquals(inRecords.size(), outRecords2.size());
        RecordSerializer recordSerializer = SerializerProvider.getSerializer(KafkaStreamProperties.EXTENDED_JSON_SERIALIZER.getValue(), null);
        for (int i = 0; i < outRecords.size(); i++) {
            ConsumerRecord<byte[], byte[]> cr = outRecords.get(i);
            ProducedKafkaRecord<byte[], byte[]> ir = inRecords.get(i);
            Record tmp = recordSerializer.deserialize(new ByteArrayInputStream(cr.value()));
            Assert.assertEquals(new String(ir.getValue()), tmp.getField(FieldDictionary.RECORD_VALUE).asString());
        }
        engineContext.getEngine().shutdown(engineContext);
        engineContext.getEngine().awaitTermination(engineContext);
    }


}