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

import kafka.server.KafkaServerStartable;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Initialises a Kafka context with an embedded zookeeper server
 * and a client... just ready for producing events
 * For online documentation
 * see
 * https://github.com/apache/kafka/blob/0.8.2/core/src/test/scala/unit/kafka/utils/TestUtils.scala
 * https://github.com/apache/kafka/blob/0.8.2/core/src/main/scala/kafka/admin/TopicCommand.scala
 * https://github.com/apache/kafka/blob/0.8.2/core/src/test/scala/unit/kafka/admin/TopicCommandTest.scala
 */
public class EmbeddedKafkaEnvironment {



    /**
     * Choose a number of random available ports
     */
    public static int[] choosePorts(int count) {
        try {
            ServerSocket[] sockets = new ServerSocket[count];
            int[] ports = new int[count];
            for (int i = 0; i < count; i++) {
                sockets[i] = new ServerSocket(0, 0, InetAddress.getByName("0.0.0.0"));
                ports[i] = sockets[i].getLocalPort();
            }
            for (int i = 0; i < count; i++)
                sockets[i].close();
            return ports;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Choose an available port
     */
    public static int choosePort() {
        return choosePorts(1)[0];
    }
    private ZkClient zkClient;

    public KafkaUnit getKafkaUnitServer() {
        return kafkaUnitServer;
    }

    private KafkaUnit kafkaUnitServer;


    /**
     * Initialises a testing Kafka environment with an EmbeddedZookeeper
     * , a client and a server
     */
    public EmbeddedKafkaEnvironment() {

        int brokerPort = choosePort();
        int zkPort = choosePort();
        kafkaUnitServer = new KafkaUnit(zkPort, brokerPort);
        kafkaUnitServer.setKafkaBrokerConfig("log.segment.bytes", "1024");
        kafkaUnitServer.startup();


        // setup Zookeeper
        zkClient = new ZkClient(kafkaUnitServer.getZkConnect(), 30000, 30000, ZKStringSerializer$.MODULE$);


    }

    /**
     * Returns a zookeeper client
     *
     * @return
     */
    public ZkClient getZkClient() {
        return zkClient;
    }


    /**
     * Returns the port
     *
     * @return
     */
    public int getBrokerPort() {
        return kafkaUnitServer.getBrokerPort();
    }

    public String getZkConnect() {
        return "localhost:" + kafkaUnitServer.getZkPort();
    }

    /**
     * Method to close all elements of EmbeddedKafkaEnvironment
     */
    public void close() throws NoSuchFieldException, IllegalAccessException {
        Field f = kafkaUnitServer.getClass().getDeclaredField("broker");
        f.setAccessible(true);
        KafkaServerStartable broker = (KafkaServerStartable) f.get(kafkaUnitServer);

        kafkaUnitServer.shutdown();
    }

}
