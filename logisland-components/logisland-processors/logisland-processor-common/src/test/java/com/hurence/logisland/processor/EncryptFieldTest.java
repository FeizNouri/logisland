package com.hurence.logisland.processor;

import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import com.hurence.logisland.util.runner.TestRunner;
import com.hurence.logisland.util.runner.TestRunners;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptFieldTest {

    private static final Logger logger = LoggerFactory.getLogger(ModifyIdTest.class);

    private Record getRecord1() {
        Record record1 = new StandardRecord();
        record1.setField("string1", FieldType.STRING, "Logisland");
        record1.setField("string2", FieldType.STRING, "Hello world !");
        return record1;
    }

    @Test
    public void testValidity() {
        final TestRunner testRunner = TestRunners.newTestRunner(new EncryptField());
        testRunner.assertValid();
        testRunner.setProperty(EncryptField.ALGO, "AES");
        testRunner.assertValid();
        testRunner.setProperty(EncryptField.MODE, EncryptField.ENCRYPT_MODE);
        testRunner.assertValid();
        testRunner.setProperty(EncryptField.KEY, "azerty1234567890");
        testRunner.assertValid();
    }

}
