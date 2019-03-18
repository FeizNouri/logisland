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
package com.hurence.logisland.service.mongodb;

import com.hurence.logisland.service.datastore.DatastoreClientService;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

public interface MongoDBClientService extends DatastoreClientService {
    default Document convertJson(String query) {
        return Document.parse(query);
    }

    long count(Document query);
    void delete(Document query);
    boolean exists(Document query);
    Document findOne(Document query);
    Document findOne(Document query, Document projection);
    List<Document> findMany(Document query);
    List<Document> findMany(Document query, int limit);
    List<Document> findMany(Document query, Document sort, int limit);
    void insert(Document doc);
    void insert(List<Document> docs);
    void update(Document query, Document update);
    void update(Document query, Document update, boolean multiple);
    void updateOne(Document query, Document update);
    void upsert(Document query, Document update);
    void dropDatabase();
    void dropCollection();

    MongoDatabase getDatabase(String name);
}
