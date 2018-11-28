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
package com.hurence.logisland.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hurence.logisland.util.string.StringUtils;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.RDD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class ConfigReader {


    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    /**
     * Loads a YAML config file (file located in the local file system)
     *
     * @param configFilePath the path of the config file
     * @return a LogislandSessionConfiguration
     * @throws Exception
     */
    public static LogislandConfiguration loadConfig(String configFilePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File configFile = new File(configFilePath);

        if (!configFile.exists()) {
            throw new FileNotFoundException("Error: File " + configFilePath + " not found!");
        }

        // replace all host from environment variables
        String fileContent = StringUtils.resolveEnvVars(readFile(configFilePath, Charset.defaultCharset()), "localhost");

        return mapper.readValue(fileContent, LogislandConfiguration.class);
    }

    /**
     * Loads a YAML config file using (file located in the shared filesystem)
     *
     * @param configFilePath the path of the config file
     * @return a LogislandSessionConfiguration
     * @throws Exception
     */
    public static LogislandConfiguration loadConfigFromSharedFS(String configFilePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());


//        /**
//         * Create a spark context just to be able to read from the shared filesystem. In databricks for instance,
//         * a path like /path/to/a/file will be loaded from DBFS so will be interpreted like dbfs:/path/to/a/file
//         */
//        SparkConf sparkConf = new SparkConf()
//                .setMaster("local") // Need at least master URL
//                .setAppName("Application for loading logisland configuration file from shared filesystem");
//        SparkContext sparkContext = new SparkContext(sparkConf);

        SparkContext sparkContext = SparkContext.getOrCreate();

        RDD<String> configRdd = sparkContext.textFile(configFilePath, 1);
        String[] configStringArray = (String[])configRdd.collect();
        String configString = String.join("\n", Arrays.asList(configStringArray));

//        sparkContext.stop();

        System.out.println("DBFS Configuration:\n" + configString);

        // replace all host from environment variables
        String fileContent = StringUtils.resolveEnvVars(configString, "localhost");

        System.out.println("Resolved Configuration:\n" + fileContent);

        return mapper.readValue(fileContent, LogislandConfiguration.class);
    }
}
