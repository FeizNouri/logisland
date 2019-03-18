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
/*
 Copyright 2016 Hurence

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.hurence.logisland.serializer;

import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;



public class NoopSerializer implements RecordSerializer {

    private static Logger logger = LoggerFactory.getLogger(NoopSerializer.class);


    /**
     * does nothing
     *
     * @param out
     * @param record
     * @throws RecordSerializationException
     */
    @Override
    public void serialize(OutputStream out, Record record) throws RecordSerializationException {
    }

    /**
     * does nothing
     *
     * @throws RecordSerializationException
     */
    @Override
    public Record deserialize(InputStream in) throws RecordSerializationException {
        return null;
    }
}
