/**
 * Copyright (C) 2019 Hurence (support@hurence.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hurence.logisland.service.influxdb;

import com.hurence.logisland.component.InitializationException;
import com.hurence.logisland.record.Field;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import com.hurence.logisland.util.runner.TestRunner;
import com.hurence.logisland.util.runner.TestRunners;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.junit.*;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.hurence.logisland.service.influxdb.InfluxDBUpdater.InfluxDBType;
import com.hurence.logisland.service.influxdb.InfluxDBControllerService.CONFIG_MODE;

import static com.hurence.logisland.service.influxdb.InfluxDBControllerService.END_OF_TEST;

@RunWith(DataProviderRunner.class)
public class InfluxDBServiceTest {

    // This test is disabled by default as it requires a docker influx db container
    // To launch this test do:
    // - Enable tests (uncomment @Test annotations)
    // - Run docker image for InfluxDB: docker run -p 8086:8086 -v influxdb:/var/lib/influxdb influxdb
    // - Run this test: "mvn -DfailIfNoTests=false [clean] test -Dtest=InfluxDBServiceTest"

    private final static String INFLUXDB_HOST = "172.17.0.2";
    private final static String INFLUXDB_PORT = "8086";
    private final static String INFLUXDB_URL = "http://" + INFLUXDB_HOST + ":" + INFLUXDB_PORT;

    private final static String TEST_DATABASE = "testDatabase";

    private static InfluxDB influxDB;

    @DataProvider
    public static Object[][] testBulkPutProvider() {

        /**
         * Measurement0 (all strings)
         *
         * testString
         *
         * this
         * is
         * a
         * simple
         * measurement
         * A last one with some spaces, UPPERCASES and a dot as well as accent and special characters: &é"'(-è_çà),;:=%ù$ãẽĩõũ.
         */

        String measurement0Name = "Measurement0";
        String timeField0 = "testTime";
        TimeUnit format0 = TimeUnit.MILLISECONDS;
        CONFIG_MODE configMode0_0 = CONFIG_MODE.ALL_AS_TAGS_BUT_EXPLICIT_FIELDS;
        Set<String> explicitTags0_0 = new HashSet<String>();
        Set<String> explicitFields0_0 = new HashSet(Arrays.asList("testString"));
        CONFIG_MODE configMode0_1 = CONFIG_MODE.ALL_AS_FIELDS;
        Set<String> explicitTags0_1 =  new HashSet<String>();
        Set<String> explicitFields0_1 = new HashSet<String>();

        List<Map<Field, InfluxDBType>> measurement0 = new ArrayList<Map<Field, InfluxDBType>>();

        Map<Field, InfluxDBType> point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTime", FieldType.STRING, "1"), InfluxDBType.STRING);
        point.put(new Field("testString", FieldType.STRING, "this"), InfluxDBType.STRING);
        measurement0.add(point);

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTime", FieldType.STRING, "2"), InfluxDBType.STRING);
        point.put(new Field("testString", FieldType.STRING, "is"), InfluxDBType.STRING);
        measurement0.add(point);

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTime", FieldType.STRING, "3"), InfluxDBType.STRING);
        point.put(new Field("testString", FieldType.STRING, "a"), InfluxDBType.STRING);
        measurement0.add(point);

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTime", FieldType.STRING, "4"), InfluxDBType.STRING);
        point.put(new Field("testString", FieldType.STRING, "simple"), InfluxDBType.STRING);
        measurement0.add(point);

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTime", FieldType.STRING, "5"), InfluxDBType.STRING);
        point.put(new Field("testString", FieldType.STRING, "measurement"), InfluxDBType.STRING);
        measurement0.add(point);

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTime", FieldType.STRING, "6"), InfluxDBType.STRING);
        point.put(new Field("testString", FieldType.STRING, "A last one with some spaces, UPPERCASES and a dot as well as accent and special characters: &é\"'(-è_çà),;:=%ù$ãẽĩõũâêîôû."), InfluxDBType.STRING);
        measurement0.add(point);

        /**
         * Measurement1 (all integers)
         *
         * testTinyint testShort testInt testLong     testBigint
         *
         * 123         12546        1563489 9623545688581  11123545688
         * -127        -4568        -954123 -8623463688247 -10128544682
         */

        String measurement1Name = "Measurement1";
        String timeField1 = "testTime";
        TimeUnit format1 = TimeUnit.MILLISECONDS;
        CONFIG_MODE configMode1 = CONFIG_MODE.EXPLICIT_TAGS_AND_FIELDS;
        Set<String> explicitTags1 = new HashSet<String>();
        Set<String> explicitFields1 = new HashSet(Arrays.asList("testTinyint", "testShort", "testInt", "testLong", "testBigint"));


        List<Map<Field, InfluxDBType>> measurement1 = new ArrayList<Map<Field, InfluxDBType>>();

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTinyint", FieldType.INT, 123), InfluxDBType.INTEGER);
        point.put(new Field("testShort", FieldType.INT, (short) 12546), InfluxDBType.INTEGER);
        point.put(new Field("testInt", FieldType.INT, 1563489), InfluxDBType.INTEGER);
        point.put(new Field("testLong", FieldType.LONG, 9623545688581L), InfluxDBType.INTEGER);
        point.put(new Field("testBigint", FieldType.LONG, new BigInteger("11123545688")), InfluxDBType.INTEGER);
        measurement1.add(point);

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTinyint", FieldType.INT, -127), InfluxDBType.INTEGER);
        point.put(new Field("testShort", FieldType.INT, (short) -4568), InfluxDBType.INTEGER);
        point.put(new Field("testInt", FieldType.INT, -954123), InfluxDBType.INTEGER);
        point.put(new Field("testLong", FieldType.LONG, -8623463688247L), InfluxDBType.INTEGER);
        point.put(new Field("testBigint", FieldType.LONG, new BigInteger("-10128544682")), InfluxDBType.INTEGER);
        measurement1.add(point);

        /**
         * Measurement2 (all floats)
         *
         * testFloat        testDouble              testDecimal
         *
         * 5984632.254893   14569874235.1254857623  477552233116699.4885451212353
         * -4712568.6423844 -74125448522.9985544221 -542212145454577.2151321145451
         */

        String measurement2Name = "Measurement2";
        String timeField2 = "testTime";
        TimeUnit format2 = TimeUnit.MILLISECONDS;
        CONFIG_MODE configMode2 = CONFIG_MODE.EXPLICIT_TAGS_AND_FIELDS;
        Set<String> explicitTags2 = new HashSet<String>();
        Set<String> explicitFields2 = new HashSet(Arrays.asList("testFloat", "testDouble", "testDecimal"));

        List<Map<Field, InfluxDBType>> measurement2 = new ArrayList<Map<Field, InfluxDBType>>();

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testFloat", FieldType.FLOAT, (float) 5984632.254893), InfluxDBType.FLOAT);
        point.put(new Field("testDouble", FieldType.DOUBLE, 14569874235.1254857623), InfluxDBType.FLOAT);
        point.put(new Field("testDecimal", FieldType.DOUBLE, new BigDecimal("477552233116699.4885451212353")), InfluxDBType.FLOAT);
        measurement2.add(point);

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testFloat", FieldType.FLOAT, (float) -4712568.6423844), InfluxDBType.FLOAT);
        point.put(new Field("testDouble", FieldType.DOUBLE, -74125448522.31225), InfluxDBType.FLOAT);
        point.put(new Field("testDecimal", FieldType.DOUBLE, new BigDecimal("-342212145454577.24565")), InfluxDBType.FLOAT);
        measurement2.add(point);

        /**
         * Measurement3 (all booleans)
         *
         * testTinyint testBoolean
         *
         * 1          true
         * 0          false
         */

        String measurement3Name = "Measurement3";
        String timeField3 = "testTime";
        TimeUnit format3 = TimeUnit.MILLISECONDS;
        CONFIG_MODE configMode3 = CONFIG_MODE.EXPLICIT_TAGS_AND_FIELDS;
        Set<String> explicitTags3 = new HashSet<String>();
        Set<String> explicitFields3 = new HashSet(Arrays.asList("testTinyint", "testBoolean"));

        List<Map<Field, InfluxDBType>> measurement3 = new ArrayList<Map<Field, InfluxDBType>>();

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTinyint", FieldType.INT, 1), InfluxDBType.BOOLEAN);
        point.put(new Field("testBoolean", FieldType.BOOLEAN, true), InfluxDBType.BOOLEAN);
        measurement3.add(point);

        point = new HashMap<Field, InfluxDBType>();
        point.put(new Field("testTinyint", FieldType.INT, 0), InfluxDBType.BOOLEAN);
        point.put(new Field("testBoolean", FieldType.BOOLEAN, false), InfluxDBType.BOOLEAN);
        measurement3.add(point);

        Object[][] inputs = {
                {measurement0, measurement0Name, timeField0, format0, configMode0_0, explicitTags0_0, explicitFields0_0},
                {measurement0, measurement0Name, timeField0, format0, configMode0_1, explicitTags0_1, explicitFields0_1},
                {measurement1, measurement1Name, timeField1, format1, configMode1, explicitTags1, explicitFields1},
                {measurement2, measurement2Name, timeField2, format2, configMode2, explicitTags2, explicitFields2},
                {measurement3, measurement3Name, timeField3, format3, configMode3, explicitTags3, explicitFields3}
        };

        return inputs;
    }

    private static void echo(String msg) {
        // Uncomment for debug
        System.out.println(msg);
    }

    @BeforeClass
    public static void connect() {

        influxDB = InfluxDBFactory.connect(INFLUXDB_URL);

        Pong pong = influxDB.ping();

        if (pong == null)
        {
            Assert.fail("Could not connect to InfluxDB");
        }
        if (!pong.isGood())
        {
            Assert.fail("Could not connect to InfluxDB (bad pong)");
        }

        echo("Connected to InfluxDB");
    }

    @AfterClass
    public static void disconnect() {
        if (influxDB != null)
            influxDB.close();
        echo("Disconnected from InfluxDB");
    }

    @Before
    public void cleanupInfluxDB() {

        // Delete database
        Query query = new Query("DROP DATABASE " + TEST_DATABASE);
        QueryResult queryResult = influxDB.query(query);
        if (queryResult.hasError())
        {
            Assert.fail("Error executing query [" +  query.getCommand() + "] : " + queryResult.getError());
        }

        // Create database

        query = new Query("CREATE DATABASE " + TEST_DATABASE);
        queryResult = influxDB.query(query);
        if (queryResult.hasError())
        {
            Assert.fail("Error executing query [" +  query.getCommand() + "] : " + queryResult.getError());
        }
        echo("InfluxDB test database cleared and prepared");
    }

    @Test
    @UseDataProvider("testBulkPutProvider")
    public void testBulkPut(List<Map<Field, InfluxDBType>> insertedAndExpectedPoints, String measurement,
                            String timeField, TimeUnit format, CONFIG_MODE configMode, Set<String> explicitTags,
                            Set<String> explicitFields)
            throws InitializationException {

        final TestRunner runner = TestRunners.newTestRunner("com.hurence.logisland.processor.datastore.BulkPut");

        final InfluxDBControllerService service = new InfluxDBControllerService();
        runner.addControllerService("influxdb_service", service);
        runner.setProperty(service, InfluxDBControllerService.URL.getName(), INFLUXDB_URL);
        runner.setProperty(service, InfluxDBControllerService.DATABASE.getName(), TEST_DATABASE);
        runner.setProperty(service, InfluxDBControllerService.MODE.getName(), configMode.toString());
        /**
         * NOTE: we always use explicit time field declaration as using record_time (which is the default if no time field
         * is specified with a measurement) will generate a lot of records with the same record_time which ends up with
         * point being overwritten in InfluxDB. To prevent that, we force usage of a time field that we use in deifferent
         * formats for testing purpose.
         */
        String explicitTagsConfigString = makeTagsConfigString(measurement, configMode, explicitTags);
        if (explicitTagsConfigString != null)
        {
            runner.setProperty(service, InfluxDBControllerService.TAGS.getName(), explicitTagsConfigString);
        }
        String explicitFieldsConfigString = makeFieldsConfigString(measurement, configMode, explicitFields);
        if (explicitFieldsConfigString != null)
        {
            runner.setProperty(service, InfluxDBControllerService.FIELDS.getName(), explicitFieldsConfigString);
        }
        runner.setProperty(service, InfluxDBControllerService.TIME_FIELD.getName(),
                makeTimeFieldConfigString(measurement, timeField, format));
        runner.setProperty(service, InfluxDBControllerService.FLUSH_INTERVAL.getName(), "1000");
        runner.setProperty(service, InfluxDBControllerService.BATCH_SIZE.getName(), "500");
        runner.enableControllerService(service);

        runner.assertNotValid();

        runner.setProperty("default.collection", "just required");
        runner.setProperty("datastore.client.service", "influxdb_service");
        runner.assertValid();

        /**
         * Bulk insert records
         */
        bulkInsert(service, insertedAndExpectedPoints, measurement);

        service.bulkPut(END_OF_TEST, new StandardRecord()); // Signal end of test
        service.waitForFlush();

        /**
         * Check measurement content
         */
        checkInfluxDBMeasurement(influxDB, insertedAndExpectedPoints, measurement, configMode, explicitTags,
                explicitFields, timeField, format);

        runner.disableControllerService(service); // Disconnect service from influxdb
    }

    /**
     * Constructs a suitable explicitTags configuration string derived from the passed measurement, config mode
     * and explicit tags
     * @param measurement
     * @param configMode
     * @param explicitTags
     */
    private static String makeTagsConfigString(String measurement, CONFIG_MODE configMode, Set<String> explicitTags) {

        switch(configMode)
        {
            case ALL_AS_FIELDS:
            case ALL_AS_TAGS_BUT_EXPLICIT_FIELDS:
                return null;
            case EXPLICIT_TAGS_AND_FIELDS:
            case ALL_AS_FIELDS_BUT_EXPLICIT_TAGS:
                if (explicitTags == null)
                    return null;
                if (explicitTags.size() == 0)
                    return null;
                return measurement + ":" + makeCsvList(explicitTags);
            default:
                Assert.fail("Unsupported config mode: " + configMode);
        }
        return null;
    }

    /**
     * Constructs a suitable explicitFields configuration string derived from the passed measurement, config mode
     * and explicit fields
     * @param measurement
     * @param configMode
     * @param explicitFields
     */
    private static String makeFieldsConfigString(String measurement, CONFIG_MODE configMode, Set<String> explicitFields) {

        switch(configMode)
        {
            case ALL_AS_FIELDS:
            case ALL_AS_FIELDS_BUT_EXPLICIT_TAGS:
                return null;
            case EXPLICIT_TAGS_AND_FIELDS:
            case ALL_AS_TAGS_BUT_EXPLICIT_FIELDS:
                if (explicitFields == null)
                    return null;
                if (explicitFields.size() == 0)
                    return null;
                return measurement + ":" + makeCsvList(explicitFields);
            default:
                Assert.fail("Unsupported config mode: " + configMode);
        }
        return null;
    }

    /**
     * Constructs a suitable timeField configuration string derived from the passed measurement, time field and format
     * @param measurement
     * @param timeField
     * @param format
     */
    private static String makeTimeFieldConfigString(String measurement, String timeField, TimeUnit format) {

        return measurement + ":" + timeField + "," + format;
    }

    /**
     * Get the expected tags from a set of similar points with the passed configuration
     * @param insertedAndExpectedPoints
     * @param configMode
     * @param explicitTags
     * @param explicitFields
     * @param timeField
     * @param format
     */
    private static Map<String, String> getExpectedTags(List<Map<Field, InfluxDBType>> insertedAndExpectedPoints,
                                                       CONFIG_MODE configMode, Set<String> explicitTags,
                                                       Set<String> explicitFields, String timeField, TimeUnit format) {

        // The first point is enough to determine the list of explicitTags (all points have the same field set and types in our test)
        Set<Field> point = insertedAndExpectedPoints.get(0).keySet();
        Map<String, String> fieldsToValues = new HashMap<String, String>();
        point.forEach(field ->
                {
                    fieldsToValues.put(field.getName(), field.getRawValue().toString());
                }
        );
        // Ignore time field
        fieldsToValues.remove(timeField);

        switch(configMode)
        {
            case ALL_AS_FIELDS:
                return new HashMap<String, String>();
            case EXPLICIT_TAGS_AND_FIELDS:
            case ALL_AS_FIELDS_BUT_EXPLICIT_TAGS:
                Map<String, String> fieldsToValuesSubset = new HashMap<String, String>();
                fieldsToValues.forEach((tag, value) ->
                        {
                            if (explicitTags.contains(tag))
                            {
                                fieldsToValuesSubset.put(tag, value);
                            }
                        }
                );
                return fieldsToValuesSubset;
            case ALL_AS_TAGS_BUT_EXPLICIT_FIELDS:
                Map<String, String> fieldsToValuesSubset2 = new HashMap<String, String>();
                fieldsToValues.forEach((tag, value) ->
                        {
                            if (!explicitFields.contains(tag))
                            {
                                fieldsToValuesSubset2.put(tag, value);
                            }
                        }
                );
                return fieldsToValuesSubset2;
            default:
                Assert.fail("Unsupported config mode: " + configMode);
        }
        return null;
    }

    /**
     * Get the expected fields from a set of similar points with the passed configuration
     * @param insertedAndExpectedPoints
     * @param configMode
     * @param explicitTags
     * @param explicitFields
     * @param timeField
     * @param format
     */
    private static List<Map<String, Object>> getExpectedFields(List<Map<Field, InfluxDBType>> insertedAndExpectedPoints,
                                                         CONFIG_MODE configMode, Set<String> explicitTags,
                                                               Set<String> explicitFields, String timeField, TimeUnit format) {

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<Field, InfluxDBType> point : insertedAndExpectedPoints)
        {
            Map<String, Object> expectedFieldsAndValues = new HashMap<String, Object>();
            for (Map.Entry<Field, InfluxDBType> entry : point.entrySet())
            {
                Field field = entry.getKey();
                InfluxDBType type = entry.getValue();

                // Ignore time field
                if (field.getName().equals(timeField))
                {
                    continue;
                }

                switch(configMode)
                {
                    case ALL_AS_FIELDS:
                        expectedFieldsAndValues.put(field.getName(), makeExpectedValue(field, type));
                        break;
                    case ALL_AS_FIELDS_BUT_EXPLICIT_TAGS:
                        if (!explicitTags.contains(field.getName()))
                        {
                            expectedFieldsAndValues.put(field.getName(), makeExpectedValue(field, type));
                        }
                        break;
                    case EXPLICIT_TAGS_AND_FIELDS:
                    case ALL_AS_TAGS_BUT_EXPLICIT_FIELDS:
                        if (explicitFields.contains(field.getName()))
                        {
                            expectedFieldsAndValues.put(field.getName(), makeExpectedValue(field, type));
                        }
                        break;
                    default:
                        Assert.fail("Unsupported config mode: " + configMode);
                }
            }
            if (expectedFieldsAndValues.size() > 0)
            {
                result.add(expectedFieldsAndValues);
            }
        }
        return result;
    }

    /**
     * Computes the expected value for a given record field which is expected to be stored with the given influx db type
     * @param field
     * @param type
     * @return
     */
    private static Object makeExpectedValue(Field field, InfluxDBType type) {

        switch (field.getType())
        {
            case INT:
                return field.asInteger();
            case LONG:
                return field.asLong();
            case FLOAT:
                return field.asFloat();
            case DOUBLE:
                return field.asDouble();
            case BOOLEAN:
                return field.asBoolean();
            case STRING:
                return field.asString();
            default:
                Assert.fail("Unsupported record field type: " + field.getType());
        }
        return null;
    }

    /**
     * Makes a comma separated value string from a list of strings.
     * Example: {"firstValue", "secondValue"} gives: firstValue,secondValue
     * @param values
     * @return
     */
    private static String makeCsvList(Set<String> values)
    {
        if (values == null)
            return "";
        if (values.size() == 0)
            return "";
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (String value : values)
        {
            if (first)
            {
                sb.append(value);
                first = false;
            } else
            {
                sb.append(",").append(value);
            }
        }
        return sb.toString();
    }

    // Adds the provided list of records to the influxdb service
    private void bulkInsert(InfluxDBControllerService service, List<Map<Field, InfluxDBType>> points, String measurement) {
        points.forEach(
                point -> {
                    service.bulkPut(measurement, rowToRecord(point));
                }
        );
    }

    // Create a record from a map of fields
    private Record rowToRecord(Map<Field, InfluxDBType> point) {

        Record record = new StandardRecord();

        point.forEach(
                (field, influxDBType) -> {
                    record.setField(field);
                }
        );

        return record;
    }

    /**
     * Checks that measurement contains the expected points
     * @param influxDB
     * @param insertedPoints
     * @param measurement
     * @param configMode
     * @param explicitTags
     * @param explicitFields
     * @param timeField
     * @param format
     */
    private void checkInfluxDBMeasurement(InfluxDB influxDB, List<Map<Field, InfluxDBType>> insertedPoints,
                                          String measurement, CONFIG_MODE configMode, Set<String> explicitTags,
                                          Set<String> explicitFields, String timeField, TimeUnit format) {

        // Need 'GROUP BY *' to get series in the measurement otherwise, cannot get tags info and all points are
        // returned in a single serie (See https://github.com/influxdata/influxdb-java/issues/101).
        Query query = new Query("SELECT * FROM " + measurement + " GROUP BY *", TEST_DATABASE);
        QueryResult queryResult = influxDB.query(query);
        if (queryResult.hasError())
        {
            Assert.fail("Error executing query [" +  query.getCommand() + "] : " + queryResult.getError());
        }

        List<Result> results = queryResult.getResults();

        Assert.assertEquals("Only one result is expected", 1, results.size());

        Result result = results.get(0);

        Map<String, String> expectedTags = getExpectedTags(insertedPoints, configMode, explicitTags, explicitFields, timeField, format);
        if (expectedTags.isEmpty())
        {
            // Serie will have null tags if none defined
            expectedTags = null;
        }
        List<Map<String, Object>> expectedPoints = getExpectedFields(insertedPoints, configMode, explicitTags, explicitFields, timeField, format);
        Assert.assertNotEquals("No expected field points", 0, expectedPoints.size());

        for (Series serie : result.getSeries())
        {
            String serieName = serie.getName();
            echo("Checking serie: " + serieName);
            Assert.assertEquals("Unexpected serie name in result", measurement, serieName);
            Map<String, String> tags = serie.getTags();
            echo("tags: " + tags);
            Assert.assertEquals("Unexpected tags in result", expectedTags, tags);

            /**
             * Compare points values with expected fields
             */
            List<String> columns = serie.getColumns();
            echo("columns: " + columns);

            // First establish a map of field column name to its index in the values (so ignore tag columns)
            Map<String, Integer> fieldToIndex = new HashMap<String, Integer>();
            for (String expectedField : expectedPoints.get(0).keySet())
            {
                int index = 0;
                boolean foundExpectedField = false;
                for (String column : columns)
                {
                    if (column.equals(expectedField))
                    {
                        // Found index of the field
                        fieldToIndex.put(expectedField, index);
                        foundExpectedField = true;
                        break;
                    } else
                    {
                        index++;
                    }
                }
                Assert.assertTrue("Expected field " + expectedField + " is not part of the influxdb columns: " + columns, foundExpectedField);
            }

            // Then find the expected point among the influxdb ones
            List<List<Object>> influxDbPoints = serie.getValues();
            Assert.assertEquals("Number of found points and expected one differ", expectedPoints.size(), influxDbPoints.size());
            for (Map<String, Object> expectedPoint : expectedPoints)
            {
                boolean foundPoint = false;
                // For each influx returned point, check each field value, if they all match, this is the expected point
                echo("Looking for expected point: " + expectedPoint);
                for (List<Object> influxPoint : influxDbPoints)
                {
                    echo("\tcomparing with influx db point: " + influxPoint);
                    int nMatchs = 0; // Number of field values that match in the current point
                    for (Map.Entry<String, Object> entry : expectedPoint.entrySet())
                    {
                        String expectedField = entry.getKey();
                        Object expectedValue = entry.getValue();
                        Assert.assertNotNull("Expected field value for field " + expectedField + " should not be null", expectedValue);
                        Integer fieldIndex = fieldToIndex.get(expectedField);
                        Assert.assertNotNull("Field " + expectedField + " not present in fieldIndex: " + fieldIndex, fieldIndex);
                        Object influxValue = influxPoint.get(fieldIndex);
                        Assert.assertNotNull("Influx field value for field " + expectedField + " should not be null", influxValue);
                        if (!expectedValue.equals(influxValue))
                        {
                            echo("\t\t-> not matching value for field " + expectedField);
                            break;
                        } else
                        {
                            echo("\t\t-> matching value for field " + expectedField);
                            nMatchs++;
                        }
                    }
                    if (nMatchs == expectedPoint.size())
                    {
                        // All field values of this point match, go to next expected point
                        echo("\t-> found matching point");
                        foundPoint = true;
                        break;
                    }
                }
                Assert.assertTrue("Could not find the following expected point in influxdb points: " + expectedPoint, foundPoint);
            }
        }
    }
}
