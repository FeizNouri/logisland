package com.hurence.logisland.processor.elasticsearch;

import com.hurence.logisland.component.InitializationException;
import com.hurence.logisland.record.FieldDictionary;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import com.hurence.logisland.util.runner.TestRunner;
import com.hurence.logisland.util.runner.TestRunners;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.hurence.logisland.processor.elasticsearch.BulkAddElasticsearch.TODAY_DATE_SUFFIX;


public class TestBulkAddElasticsearch {

    private volatile Map<String/*id*/, String/*errors*/> errors = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(TestBulkAddElasticsearch.class);

    @Test
    public void testPutElasticsearchTwoRecords() throws IOException, InitializationException {

        final String DEFAULT_INDEX = "test_index";
        final String DEFAULT_TYPE = "cisco_record";
        final String ES_INDEX_FIELD = "index_field";
        final String ES_TYPE_FIELD = "type_field";

        //////////////////
        final TestRunner runner = TestRunners.newTestRunner(BulkAddElasticsearch.class);
        runner.setProperty(BulkAddElasticsearch.DEFAULT_INDEX, DEFAULT_INDEX);
        runner.setProperty(BulkAddElasticsearch.DEFAULT_TYPE, DEFAULT_TYPE);
        runner.setProperty(BulkAddElasticsearch.TIMEBASED_INDEX, TODAY_DATE_SUFFIX);
        runner.setProperty(BulkAddElasticsearch.ES_INDEX_FIELD, ES_INDEX_FIELD);
        runner.setProperty(BulkAddElasticsearch.ES_TYPE_FIELD, ES_TYPE_FIELD);
        runner.setProperty(BulkAddElasticsearch.ELASTICSEARCH_CLIENT_SERVICE, "elasticsearchClient");

        runner.assertValid();

        ///////////////////
        final MockElasticsearchClientService elasticsearchClient = new MockElasticsearchClientService();
        runner.addControllerService("elasticsearchClient", elasticsearchClient);
        runner.enableControllerService(elasticsearchClient);

        ///////////////////
        final Record inputRecord1 = new StandardRecord(DEFAULT_TYPE)
                .setId("firewall_record0")
                .setField(FieldDictionary.RECORD_TIME, FieldType.LONG, 1475525688668L)
                .setField("method", FieldType.STRING, "GET")
                .setField("ip_source", FieldType.STRING, "123.34.45.123")
                .setField("ip_target", FieldType.STRING, "255.255.255.255")
                .setField("url_scheme", FieldType.STRING, "http")
                .setField("url_host", FieldType.STRING, "origin-www.20minutes.fr")
                .setField("url_port", FieldType.STRING, "80")
                .setField("url_path", FieldType.STRING, "/r15lgc-100KB.js")
                .setField("request_size", FieldType.INT, 1399)
                .setField("response_size", FieldType.INT, 452)
                .setField("is_outside_office_hours", FieldType.BOOLEAN, false)
                .setField("is_host_blacklisted", FieldType.BOOLEAN, false)
                .setField("tags", FieldType.ARRAY, new ArrayList<>(Arrays.asList("spam", "filter", "mail")));

        final Record inputRecord2 = new StandardRecord(DEFAULT_TYPE)
                .setId("firewall_record1")
                .setField(FieldDictionary.RECORD_TIME, FieldType.LONG, 1475525688668L)
                .setField("method", FieldType.STRING, "GET")
                .setField("ip_source", FieldType.STRING, "123.34.45.12")
                .setField("ip_target", FieldType.STRING, "255.255.255.255")
                .setField("url_scheme", FieldType.STRING, "http")
                .setField("url_host", FieldType.STRING, "origin-www.20minutes.fr")
                .setField("url_port", FieldType.STRING, "80")
                .setField("url_path", FieldType.STRING, 45)
                .setField("request_size", FieldType.INT, 1399)
                .setField("response_size", FieldType.INT, 452)
                .setField("is_outside_office_hours", FieldType.BOOLEAN, false)
                .setField("is_host_blacklisted", FieldType.BOOLEAN, false)
                .setField("tags", FieldType.ARRAY, new ArrayList<>(Arrays.asList("spam", "filter", "mail")));

        runner.enqueue(inputRecord1);
        runner.enqueue(inputRecord2);
        runner.clearQueues();
        runner.run();
        runner.assertAllInputRecordsProcessed();
        runner.assertOutputRecordsCount(2);
        runner.assertOutputErrorCount(0);
        elasticsearchClient.flushBulkProcessor();

        try {
            elasticsearchClient.refreshIndex(DEFAULT_INDEX);
            Assert.assertEquals(2, elasticsearchClient.countIndex(DEFAULT_INDEX));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //long numberOfHits = elasticsearchClient.searchNumberOfHits(DEFAULT_INDEX, DEFAULT_TYPE, "ip_source", "123.34.45.123");
        //Assert.assertEquals(1,numberOfHits);

    }

}