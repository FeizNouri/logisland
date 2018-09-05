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
package com.hurence.logisland.service.cassandra;

import com.datastax.driver.core.*;
import com.hurence.logisland.component.InitializationException;
import com.hurence.logisland.record.Field;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import com.hurence.logisland.util.runner.TestRunner;
import com.hurence.logisland.util.runner.TestRunners;
import com.hurence.logisland.service.cassandra.RecordConverter.CassandraType;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.*;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public class CassandraServiceTest {

    // Embedded cassandra server maven plugin instance connect info

    // TODO: set back cassandra maven plugin usage
    private final static String CASSANDRA_HOST = "localhost";
    private final static String CASSANDRA_PORT = "19042";

    // Use these ones instead if you want to use the "docker cassandra start" instead of embedded cassandra server maven plugin instance
    // Or use "mvn -DfailIfNoTests=false [clean] test -Dtest=CassandraServiceTest" If want to use embedded
    // cassandra server maven plugin running only this test out of the IDE
//    private final static String CASSANDRA_HOST = "172.17.0.4"; // Set the right docker container ip
//    private final static String CASSANDRA_PORT = "9042";

    private final static String TEST_KEYSPACE = "testKeySpace";
    private final static String TEST_TABLE = "testTable";

    private static Cluster cluster;
    private static Session session;

    @DataProvider
    public static Object[][] testBulkPutProvider() {

        /**
         * Table0 (simplest, text)
         *
         * testText
         *
         * hello
         * this
         * is
         * a
         * simple
         * table
         * with
         * some
         * text
         * values
         * A last one with some spaces, UPPERCASES and a dot as well as accent and special characters: &é"'(-è_çà),;:=%ù$ãẽĩõũ.
         */

        String tableFields0 = "testText:text";
        String tablePrimaryKey0 = "testText";

        List<Map<Field, CassandraType>> table0 = new ArrayList<Map<Field, CassandraType>>();
        Map<Field, CassandraType> row = new HashMap<Field, CassandraType>();

        row.put(new Field("testText", FieldType.STRING, "hello"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "this"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "is"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "a"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "simple"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "table"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "with"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "some"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "text"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "values"), CassandraType.TEXT);
        table0.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testText", FieldType.STRING, "A last one with some spaces, UPPERCASES and a dot as well as accent and special characters: &é\"'(-è_çà),;:=%ù$ãẽĩõũâêîôû."), CassandraType.TEXT);
        table0.add(row);

        /**
         * Table1 (simple, simple primary key, uuid)
         *
         * testUuid                             testInt
         *
         * d6328472-b571-4f61-a82e-fe4344228291 215461
         * d6328472-b571-4f61-a82e-fe4344228292 215462
         */

        String tableFields1 = "testUuid:uuid,testInt:int";
        String tablePrimaryKey1 = "testUuid";

        List<Map<Field, CassandraType>> table1 = new ArrayList<Map<Field, CassandraType>>();
        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testUuid", FieldType.STRING, "d6328472-b571-4f61-a82e-fe4344228291"), CassandraType.UUID);
        row.put(new Field("testInt", FieldType.INT, 215461), CassandraType.INT);
        table1.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testUuid", FieldType.STRING, "d6328472-b571-4f61-a82e-fe4344228292"), CassandraType.UUID);
        row.put(new Field("testInt", FieldType.INT, 215462), CassandraType.INT);
        table1.add(row);

        /**
         * Table2 (simple, composite primary key)
         *
         * testUuid                             testInt testFloat     testSmallint
         *
         * d6328472-b571-4f61-a82e-fe4344228291 215461  123.456       12546
         * d6328472-b571-4f61-a82e-fe4344228292 215462  456789.123456 -4568
         */

        String tableFields2 = "testUuid:uuid,testInt:int,testFloat:float,testSmallint:smallint";
        String tablePrimaryKey2 = "testUuid,testInt,testSmallint";

        List<Map<Field, CassandraType>> table2 = new ArrayList<Map<Field, CassandraType>>();
        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testUuid", FieldType.STRING, "d6328472-b571-4f61-a82e-fe4344228291"), CassandraType.UUID);
        row.put(new Field("testInt", FieldType.INT, 215461), CassandraType.INT);
        row.put(new Field("testFloat", FieldType.FLOAT, (float)123.456), CassandraType.FLOAT);
        row.put(new Field("testSmallint", FieldType.INT, 12546), CassandraType.SMALLINT);
        table2.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testUuid", FieldType.STRING, "d6328472-b571-4f61-a82e-fe4344228292"), CassandraType.UUID);
        row.put(new Field("testInt", FieldType.INT, 215462), CassandraType.INT);
        row.put(new Field("testFloat", FieldType.FLOAT, (float)456789.123456), CassandraType.FLOAT);
        row.put(new Field("testSmallint", FieldType.INT, -4568), CassandraType.SMALLINT);
        table2.add(row);

        /**
         * Table3 (all integers)
         *
         * testTinyint testSmallint testInt testBigint     testVarint
         *
         * 123         12546        1563489 9623545688581  11123545688
         * -127        -4568        -954123 -8623463688247 -10128544682
         */

        String tableFields3 = "testTinyint:tinyint,testSmallint:smallint,testInt:int,testBigint:bigint,testVarint:varint";
        String tablePrimaryKey3 = "testTinyint";

        List<Map<Field, CassandraType>> table3 = new ArrayList<Map<Field, CassandraType>>();
        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testTinyint", FieldType.INT, 123), CassandraType.TINYINT);
        row.put(new Field("testSmallint", FieldType.INT, (short)12546), CassandraType.SMALLINT);
        row.put(new Field("testInt", FieldType.INT, 1563489), CassandraType.INT);
        row.put(new Field("testBigint", FieldType.LONG, 9623545688581L), CassandraType.BIGINT);
        row.put(new Field("testVarint", FieldType.LONG, new BigInteger("11123545688")), CassandraType.VARINT);
        table3.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testTinyint", FieldType.INT, -127), CassandraType.TINYINT);
        row.put(new Field("testSmallint", FieldType.INT, (short)-4568), CassandraType.SMALLINT);
        row.put(new Field("testInt", FieldType.INT, -954123), CassandraType.INT);
        row.put(new Field("testBigint", FieldType.LONG, -8623463688247L), CassandraType.BIGINT);
        row.put(new Field("testVarint", FieldType.LONG, new BigInteger("-10128544682")), CassandraType.VARINT);
        table3.add(row);

        /**
         * Table4 (all decimals)
         *
         * testFloat        testDouble              testDecimal
         *
         * 5984632.254893   14569874235.1254857623  477552233116699.4885451212353
         * -4712568.6423844 -74125448522.9985544221 -542212145454577.2151321145451
         */

        String tableFields4 = "testFloat:float,testDouble:double,testDecimal:decimal";
        String tablePrimaryKey4 = "testFloat";

        List<Map<Field, CassandraType>> table4 = new ArrayList<Map<Field, CassandraType>>();
        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testFloat", FieldType.FLOAT, (float)5984632.254893), CassandraType.FLOAT);
        row.put(new Field("testDouble", FieldType.DOUBLE, 14569874235.1254857623), CassandraType.DOUBLE);
        row.put(new Field("testDecimal", FieldType.DOUBLE, new BigDecimal("477552233116699.4885451212353")), CassandraType.DECIMAL);
        table4.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testFloat", FieldType.FLOAT, (float)-4712568.6423844), CassandraType.FLOAT);
        row.put(new Field("testDouble", FieldType.DOUBLE, -74125448522.31225), CassandraType.DOUBLE);
        row.put(new Field("testDecimal", FieldType.DOUBLE, new BigDecimal("-342212145454577.24565")), CassandraType.DECIMAL);
        table4.add(row);

        /**
         * Table5 (blob and boolean)
         *
         * testTinyint testBoolean testBlob
         *
         * 1           true        this is a blob
         * -2          false       {0x0a, 0x02, 0xff}
         */

        String tableFields5 = "testTinyint:tinyint,testBoolean:boolean,testBlob:blob";
        String tablePrimaryKey5 = "testTinyint";

        List<Map<Field, CassandraType>> table5 = new ArrayList<Map<Field, CassandraType>>();
        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testTinyint", FieldType.INT, 1), CassandraType.TINYINT);
        row.put(new Field("testBoolean", FieldType.BOOLEAN, true), CassandraType.BOOLEAN);
        row.put(new Field("testBlob", FieldType.BYTES, "this is a blob".getBytes()), CassandraType.BLOB);
        table5.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testTinyint", FieldType.INT, -2), CassandraType.TINYINT);
        row.put(new Field("testBoolean", FieldType.BOOLEAN, false), CassandraType.BOOLEAN);
        row.put(new Field("testBlob", FieldType.BYTES, new byte[] {0xa, 0x2, (byte) 0xff}), CassandraType.BLOB);
        table5.add(row);

        /**
         * Table6 (all date and times)
         *
         * testTimestamp                testDate   testTime
         *
         * 1299038700000                7892       57000000000
         * 2011-02-03T04:05:00.000+0000 2011-02-03 08:12:54.123456789
         */

        String tableFields6 = "testTimestamp:timestamp,testDate:date,testTime:time";
        String tablePrimaryKey6 = "testTimestamp";

        List<Map<Field, CassandraType>> table6 = new ArrayList<Map<Field, CassandraType>>();
        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testTimestamp", FieldType.LONG, 1299038700000L), CassandraType.TIMESTAMP);
        row.put(new Field("testDate", FieldType.LONG, 7892L), CassandraType.DATE);
        row.put(new Field("testTime", FieldType.LONG, 57000000000L), CassandraType.TIME);
        table6.add(row);

        row = new HashMap<Field, CassandraType>();
        row.put(new Field("testTimestamp", FieldType.STRING, "2011-02-03T04:05:00.000+0000"), CassandraType.TIMESTAMP);
        row.put(new Field("testDate", FieldType.STRING, "2011-02-03"), CassandraType.DATE);
        row.put(new Field("testTime", FieldType.STRING, "08:12:54.523456789"), CassandraType.TIME);
        table6.add(row);

        Object[][] inputs = {
                {table0, tableFields0, tablePrimaryKey0, "table0"},
                {table1, tableFields1, tablePrimaryKey1, "table1"},
                {table2, tableFields2, tablePrimaryKey2, "table2"},
                {table3, tableFields3, tablePrimaryKey3, "table3"},
                {table4, tableFields4, tablePrimaryKey4, "table4"},
                {table5, tableFields5, tablePrimaryKey5, "table5"},
                {table6, tableFields6, tablePrimaryKey6, "table6"}
        };

        return inputs;
    }

    private static void echo(String msg)
    {
        // Uncomment for debug
//        System.out.println(msg);
    }

    @BeforeClass
    public static void connect()
    {
        Cluster.Builder builder = Cluster.builder();
        builder.addContactPoint(CASSANDRA_HOST).withPort(Integer.valueOf(CASSANDRA_PORT));
        cluster = builder.build();
        session = cluster.connect();
        echo("Connected to Cassandra");
    }

    @AfterClass
    public static void disconnect()
    {
        session.close();
        cluster.close();
        echo("Disconnected from Cassandra");
    }

    @Before
    public void cleanupCassandra()
    {
        /**
         * Delete table
         */
        StringBuffer sb = new StringBuffer("DROP TABLE IF EXISTS " + TEST_KEYSPACE + "." + TEST_TABLE);
        String statement = sb.toString();
        ResultSet resultSet = session.execute(sb.toString());
        if (!resultSet.wasApplied())
        {
            Assert.fail("Statement not applied: " + statement);
        }

        /**
         * Delete keyspace
         */
        sb = new StringBuffer("DROP KEYSPACE IF EXISTS " + TEST_KEYSPACE);
        statement = sb.toString();
        resultSet = session.execute(sb.toString());
        if (!resultSet.wasApplied())
        {
            Assert.fail("Statement not applied: " + statement);
        }

        echo("Cassandra DB cleared");
    }

    @Test
    @UseDataProvider("testBulkPutProvider")
    public void testBulkPut(List<Map<Field, CassandraType>> insertedAndExpectedRows, String tableFields,
                            String tablePrimaryKey, String tableName) throws InitializationException {

        final TestRunner runner = TestRunners.newTestRunner("com.hurence.logisland.processor.datastore.BulkPut");

        final CassandraControllerService service = new CassandraControllerService();
        runner.setProperty(CassandraControllerService.HOSTS.getName(), CASSANDRA_HOST);
        runner.setProperty(CassandraControllerService.PORT.getName(), CASSANDRA_PORT);
        runner.setProperty(CassandraControllerService.KEYSPACE.getName(), TEST_KEYSPACE);
        runner.setProperty(CassandraControllerService.TABLE.getName(), TEST_TABLE);
        runner.setProperty(CassandraControllerService.TABLE_FIELDS.getName(), tableFields);
        runner.setProperty(CassandraControllerService.TABLE_PRIMARY_KEY.getName(), tablePrimaryKey);
        runner.setProperty(CassandraControllerService.CREATE_SCHEMA.getName(), "true");
        runner.setProperty(CassandraControllerService.FLUSH_INTERVAL.getName(), "2000");
        runner.setProperty(CassandraControllerService.BATCH_SIZE.getName(), "500");
        runner.addControllerService("cassandra_service", service);
        runner.enableControllerService(service);

        bulkInsert(service, insertedAndExpectedRows);

        checkCassandraTable(session, insertedAndExpectedRows, tableName);

        runner.disableControllerService(service); // Disconnect service from cassandra
    }

    // Adds the provided list of records to the cassandra service
    private void bulkInsert(CassandraControllerService service, List<Map<Field, CassandraType>> rows)
    {
        rows.forEach(
                row -> {
                    service.bulkPut(null, rowToRecord(row));
                }
        );

        service.waitForFlush();
    }

    // Create a record from a map of fields
    private Record rowToRecord(Map<Field, CassandraType> row)
    {

        Record record = new StandardRecord();

        row.forEach(
                (field, cassandraType) -> {
                    record.setField(field);
                }
        );

        return record;
    }

    // Checks that table contains the expected lines
    private void checkCassandraTable(Session session, List<Map<Field, CassandraType>> expectedRows, String tableName)
    {
        StringBuffer sb = new StringBuffer("SELECT * FROM " + TEST_KEYSPACE + "." + TEST_TABLE);
        String statement = sb.toString();
        ResultSet resultSet = session.execute(sb.toString());
        if (!resultSet.wasApplied())
        {
            Assert.fail("Statement not applied: " + statement);
        }

        assertEquals("Number of found lines in table " + tableName + " is not equal to expected ones", expectedRows.size(), resultSet.getAvailableWithoutFetching());

        // Now check that lines are all expected ones
        Iterator<Row> iterator = resultSet.iterator();
        int nRows = 0;
        while(iterator.hasNext())
        {
            Row row = iterator.next();
            echo("Trying to find matching expected row for row: " + row);
            findRow(row, expectedRows, tableName);
            nRows++;
        }
    }

    // Checks that a cassandra row is in the expected ones list
    private void findRow(Row actualRow, List<Map<Field, CassandraType>> expectedRows, String tableName)
    {
        for (Map<Field, CassandraType> expectedRow: expectedRows) {

            // Does the returned cassandra row match this current expected one?
            try {
                FieldType fieldType;
                for (Map.Entry<Field, CassandraType> entry : expectedRow.entrySet()) {
                    Field field = entry.getKey();
                    String fieldName = field.getName();
                    CassandraType cassandraType = entry.getValue();
                    switch (cassandraType) {
                        case UUID:
                            UUID actualUuid = actualRow.getUUID(fieldName);
                            UUID expectedUuid = UUID.fromString(field.asString());
                            if (!expectedUuid.equals(actualUuid))
                            {
                                throw new Exception("In table " + tableName + ", uuid values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra uuid value is " + actualUuid);
                            }
                            break;
                        case TEXT:
                            String actualString = actualRow.getString(fieldName);
                            String expectedString = field.asString();
                            if (!expectedString.equals(actualString))
                            {
                                throw new Exception("In table " + tableName + ", text values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra text value is " + actualString);
                            }
                            break;
                        case DATE:
                            LocalDate actualDate = actualRow.getDate(fieldName);
                            LocalDate expectedDate;
                            fieldType = field.getType();
                            if (fieldType == FieldType.STRING)
                            {
                                String expectedDateString = field.asString();
                                /**
                                 * yyyy-mm-dd (so '2011-02-03')
                                 */
                                expectedDate = RecordConverter.cassandraDateToLocalDate(expectedDateString);
                            } else
                            {
                                expectedDate = LocalDate.fromDaysSinceEpoch(field.asLong().intValue());
                            }
                            if (!expectedDate.equals(actualDate))
                            {
                                throw new Exception("In table " + tableName + ", date values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra date value is " + actualDate);
                            }
                            break;
                        case TIME:
                            Long actualTime = actualRow.getTime(fieldName);
                            Long expectedTime;
                            fieldType = field.getType();
                            if (fieldType == FieldType.STRING)
                            {
                                String expectedTimeString = field.asString();
                                /**
                                 * hh:mm:ss[.fffffffff] (where the sub-second precision is optional and if provided, can be less than the nanosecond).
                                 * So for instance, the following are valid inputs for a time:
                                 *
                                 *     '08:12:54'
                                 *     '08:12:54.123'
                                 *     '08:12:54.123456'
                                 *     '08:12:54.123456789'
                                 */
                                expectedTime = RecordConverter.cassandraTimeToNanosecondsSinceMidnight(expectedTimeString);
                            } else
                            {
                                expectedTime = field.asLong();

                            }
                            if (!expectedTime.equals(actualTime))
                            {
                                throw new Exception("In table " + tableName + ", time values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra time value is " + actualTime);
                            }
                            break;
                        case TIMESTAMP:
                            Date actualTimestamp = actualRow.getTimestamp(fieldName);
                            Date expectedTimestamp;
                            fieldType = field.getType();
                            if (fieldType == FieldType.STRING)
                            {
                                String expectedTimestampString = field.asString();
                                /**
                                 * String that represents an ISO 8601 date. For instance, all of the values below are valid timestamp values for Mar 2, 2011, at 04:05:00 AM, GMT:
                                 *
                                 *     1299038700000
                                 *     '2011-02-03 04:05+0000'
                                 *     '2011-02-03 04:05:00+0000'
                                 *     '2011-02-03 04:05:00.000+0000'
                                 *     '2011-02-03T04:05+0000'
                                 *     '2011-02-03T04:05:00+0000'
                                 *     '2011-02-03T04:05:00.000+0000'
                                 */
                                expectedTimestamp = RecordConverter.cassandraTimestampToDate(expectedTimestampString);
                            } else
                            {
                                expectedTimestamp = new Date(field.asLong());
                            }
                            if (!expectedTimestamp.equals(actualTimestamp))
                            {
                                throw new Exception("In table " + tableName + ", timestamp values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra timestamp value is " + actualTimestamp);
                            }
                            break;
                        case TINYINT:
                            Byte actualByte = actualRow.getByte(fieldName);
                            Byte expectedByte = new Byte(field.asInteger().toString());
                            if (!expectedByte.equals(actualByte))
                            {
                                throw new Exception("In table " + tableName + ", tinyint values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra tinyint value is " + actualByte);
                            }
                            break;
                        case SMALLINT:
                            Short actualShort = actualRow.getShort(fieldName);
                            Short expectedShort = new Short(field.asInteger().toString());
                            if (!expectedShort.equals(actualShort))
                            {
                                throw new Exception("In table " + tableName + ", smallint values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra smallint value is " + actualShort);
                            }
                            break;
                        case INT:
                            Integer actualInteger = actualRow.getInt(fieldName);
                            Integer expectedInteger = field.asInteger();
                            if (!expectedInteger.equals(actualInteger))
                            {
                                throw new Exception("In table " + tableName + ", int values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra int value is " + actualInteger);
                            }
                            break;
                        case BIGINT:
                            Long actualLong = actualRow.getLong(fieldName);
                            Long expectedLong = field.asLong();
                            if (!expectedLong.equals(actualLong))
                            {
                                throw new Exception("In table " + tableName + ", bigint values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra bigint value is " + actualLong);
                            }
                            break;
                        case VARINT:
                            BigInteger actualBigInteger = actualRow.getVarint(fieldName);
                            BigInteger expectedBigInteger = BigInteger.valueOf(field.asLong());
                            if (!expectedBigInteger.equals(actualBigInteger))
                            {
                                throw new Exception("In table " + tableName + ", varint values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra varint value is " + actualBigInteger);
                            }
                            break;
                        case FLOAT:
                            Float actualFloat = actualRow.getFloat(fieldName);
                            Float expectedFloat = field.asFloat();
                            if (!expectedFloat.equals(actualFloat))
                            {
                                throw new Exception("In table " + tableName + ", float values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra float value is " + actualFloat);
                            }
                            break;
                        case DOUBLE:
                            Double actualDouble = actualRow.getDouble(fieldName);
                            Double expectedDouble = field.asDouble();
                            if (!expectedDouble.equals(actualDouble))
                            {
                                throw new Exception("In table " + tableName + ", double values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra double value is " + actualDouble);
                            }
                            break;
                        case DECIMAL:
                            BigDecimal actualBigDecimal = actualRow.getDecimal(fieldName);
                            BigDecimal expectedBigDecimal = BigDecimal.valueOf(field.asDouble());
                            if (!expectedBigDecimal.equals(actualBigDecimal))
                            {
                                echo("actualBigDecimal="+actualBigDecimal);
                                echo("expectedBigDecimal="+expectedBigDecimal);
                                throw new Exception("In table " + tableName + ", decimal values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra decimal value is " + actualBigDecimal);
                            }
                            break;
                        case BOOLEAN:
                            Boolean actualBoolean = actualRow.getBool(fieldName);
                            Boolean expectedBoolean = field.asBoolean();
                            if (!expectedBoolean.equals(actualBoolean))
                            {
                                throw new Exception("In table " + tableName + ", boolean values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra boolean value is " + actualBoolean);
                            }
                            break;
                        case BLOB:
                            ByteBuffer actualByteBuffer = actualRow.getBytes(fieldName);
                            Object rawValue = field.getRawValue();
                            byte[] bytes = (byte[])rawValue;
                            ByteBuffer expectedByteBuffer = ByteBuffer.wrap(bytes);
                            if (!expectedByteBuffer.equals(actualByteBuffer))
                            {
                                throw new Exception("In table " + tableName + ", blob values for field " + fieldName +
                                        " differ for expected row: " + expectedRow + ". Cassandra blob value is " + actualByteBuffer);
                            }
                            break;
                        default:
                            Assert.fail("Unsupported cassandra type: " + cassandraType);
                    }
                }

                // Ok found this row in the expected ones
                echo("Found a matching row for " + actualRow);
                return;
            } catch(Exception e)
            {
                // Does not match this row, let's try the next one
                echo("Not this row: " + e.getMessage());
            }
        }

        Assert.fail("Unable to find this row in the expected ones: " + actualRow);
    }
}
