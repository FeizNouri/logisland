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
package com.hurence.logisland.processor.alerting;

import com.hurence.logisland.component.InitializationException;
import com.hurence.logisland.processor.datastore.MockDatastoreService;
import com.hurence.logisland.record.FieldDictionary;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import com.hurence.logisland.service.datastore.DatastoreClientService;
import com.hurence.logisland.util.runner.TestRunner;
import com.hurence.logisland.util.runner.TestRunners;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class ComputeTagsTest {

    @Test
    public void testMultipleRules() throws InitializationException {

        // create the controller service and link it to the test processor
        final DatastoreClientService service = new MockDatastoreService();
        getCacheRecords().forEach(r -> service.put("test", r, false));

        final TestRunner runner = TestRunners.newTestRunner(ComputeTag.class);
        runner.setProperty(ComputeTag.MAX_CPU_TIME, "100");
        runner.setProperty(ComputeTag.MAX_MEMORY, "12800000");
        runner.setProperty(ComputeTag.MAX_PREPARED_STATEMENTS, "100");
        runner.setProperty(ComputeTag.ALLOw_NO_BRACE, "false");
        runner.setProperty("cvib3", "return 37.2/10*3;");
        runner.setProperty("cvib2", "if( cache(\"cached_id2\").value > 10 ) return 0.0; else return 1.0;");
        runner.setProperty("cvib1", "return cache(\"cached_id1\").value * 10.2;");
        runner.setProperty(ComputeTag.DATASTORE_CLIENT_SERVICE, service.getIdentifier());
        runner.addControllerService(service.getIdentifier(), service);
        runner.enableControllerService(service);

        final DatastoreClientService lookupService = runner.getProcessContext()
                .getPropertyValue(ComputeTag.DATASTORE_CLIENT_SERVICE)
                .asControllerService(MockDatastoreService.class);

        Collection<Record> recordsToEnrich = getRecords();
        runner.assertValid();
        runner.enqueue(recordsToEnrich);
        runner.run();
        runner.assertAllInputRecordsProcessed();
        runner.assertOutputRecordsCount(3);
        runner.assertOutputErrorCount(0);

        for (Record enriched : runner.getOutputRecords()) {
            if (enriched.getId().equals("cvib1")) {
                assertEquals(enriched.getField(FieldDictionary.RECORD_VALUE).asDouble(), 10.2 * 12.45, 0.0001);
            } else if (enriched.getId().equals("cvib2")) {
                assertEquals(enriched.getField(FieldDictionary.RECORD_VALUE).asDouble(), 1.0, 0.00001);
            } else if (enriched.getId().equals("cvib3")) {
                assertEquals(enriched.getField(FieldDictionary.RECORD_VALUE).asDouble(), 37.2 / 10.0 * 3.0, 0.00001);
            }
        }
    }


    @Test
    public void testBadRules() throws InitializationException {

        // create the controller service and link it to the test processor
        final DatastoreClientService service = new MockDatastoreService();
        getCacheRecords().forEach(r -> service.put("test", r, false));

        final TestRunner runner = TestRunners.newTestRunner(ComputeTag.class);
        runner.setProperty(ComputeTag.MAX_CPU_TIME, "100");
        runner.setProperty(ComputeTag.MAX_MEMORY, "12800000");
        runner.setProperty(ComputeTag.MAX_PREPARED_STATEMENTS, "100");
        runner.setProperty(ComputeTag.ALLOw_NO_BRACE, "false");
        runner.setProperty("cvib3", "return 37.2/++10*3;");
       /* runner.setProperty("cvib2", "if( cache(\"cached_id2\").value > 10 ) return 0.0; else return 1.0;");
        runner.setProperty("cvib1", "return cache(\"cached_id1\").value * 10.2;");*/
        runner.setProperty(ComputeTag.DATASTORE_CLIENT_SERVICE, service.getIdentifier());
        runner.addControllerService(service.getIdentifier(), service);
        runner.enableControllerService(service);

        final DatastoreClientService lookupService = runner.getProcessContext()
                .getPropertyValue(ComputeTag.DATASTORE_CLIENT_SERVICE)
                .asControllerService(MockDatastoreService.class);

        Collection<Record> recordsToEnrich = getRecords();
        runner.assertValid();
        runner.enqueue(recordsToEnrich);
        runner.run();
        runner.assertAllInputRecordsProcessed();
        runner.assertOutputRecordsCount(0);
        runner.assertOutputErrorCount(1);

        for (Record enriched : runner.getOutputRecords()) {
            if (enriched.getId().equals("cvib3")) {
                assertEquals(enriched.getErrors().toArray()[0],
                        "ScriptException: <eval>:3:17 Invalid left hand side for assignment\n" +
                        " return 37.2 / ++10 * 3;\n" +
                        "                 ^ in <eval> at line number 3 at column number 17");
            }
        }
    }

    private Collection<Record> getRecords() {
        Collection<Record> recordsToEnrich = new ArrayList<>();

        recordsToEnrich.add(new StandardRecord()
                .setId("id1")
                .setField("a", FieldType.STRING, "a1")
                .setField("b", FieldType.STRING, "b1")
                .setField("c", FieldType.LONG, 1));

        recordsToEnrich.add(new StandardRecord()
                .setId("id2")
                .setField("a", FieldType.STRING, "a2")
                .setField("b", FieldType.STRING, "b2")
                .setField("c", FieldType.LONG, 2));

        recordsToEnrich.add(new StandardRecord()
                .setId("id3")
                .setField("a", FieldType.STRING, "a3")
                .setField("b", FieldType.STRING, "b3")
                .setField("c", FieldType.LONG, 3));
        return recordsToEnrich;
    }


    private Collection<Record> getCacheRecords() {
        Collection<Record> lookupRecords = new ArrayList<>();

        lookupRecords.add(new StandardRecord()
                .setId("cached_id1")
                .setField(FieldDictionary.RECORD_VALUE, FieldType.DOUBLE, 12.45));

        lookupRecords.add(new StandardRecord()
                .setId("cached_id2")
                .setField(FieldDictionary.RECORD_VALUE, FieldType.DOUBLE, 2.5));

        return lookupRecords;
    }
}
