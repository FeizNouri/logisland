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
package com.hurence.logisland.service.mongodb;


import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * this class converts a logisland Record to a BSON document back and fort
 */
public class RecordConverter {

    public static final String MONGO_DOC_TYPE = "mongo_document";

    public static Record convert(Document document){
        Record record = new StandardRecord(MONGO_DOC_TYPE)
                .setId(document.getObjectId("_id").toString());

        return record;
    }


    public static Document convert(Record record){
        Document document = new Document("_id", new ObjectId(record.getId()));

        return document;
    }
}
