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
package com.hurence.logisland.util.kafka;

/*
 * Copyright (C) 2014 Christopher Batey
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


import static org.junit.Assert.assertEquals;

public class KafkaUnitIntegrationTest {

   /* @Rule
    public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule(40000, 40001);

    @Test
    public void junitRuleShouldHaveStartedKafka() throws Exception {
        //given
        String testTopic = "TestTopic";
        kafkaUnitRule.getKafkaUnit().createTopic(testTopic);
        KeyedMessage<String, String> keyedMessage = new KeyedMessage<>(testTopic, "key", "value");

        //when
        kafkaUnitRule.getKafkaUnit().sendMessages(keyedMessage);
        List<String> messages = kafkaUnitRule.getKafkaUnit().readMessages(testTopic, 1);

        //then
        assertEquals(Collections.singletonList("value"), messages);

    }*/
}