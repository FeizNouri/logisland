/**
 * Copyright (C) 2017 Hurence (support@hurence.com)
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
package com.hurence.logisland.processor.commonlogs.gitlab;

import com.hurence.logisland.annotation.documentation.CapabilityDescription;
import com.hurence.logisland.annotation.documentation.Tags;
import com.hurence.logisland.component.PropertyDescriptor;
import com.hurence.logisland.processor.*;
import com.hurence.logisland.record.Field;
import com.hurence.logisland.record.FieldDictionary;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.util.string.JsonUtil;
import com.hurence.logisland.validator.StandardValidators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gitlab logs processor
 */
@Tags({"logs", "gitlab"})
@CapabilityDescription(
        "The Gitlab logs processor is the Logisland entry point to get and process `Gitlab <https://www.gitlab.com>`_ logs."
        + "Basically the events coming from Gitlab are JSON documents")
public class ParseGitlabLog extends AbstractProcessor {

    private static Logger logger = LoggerFactory.getLogger(ParseGitlabLog.class);

    private boolean debug = false;
    
    private static final String KEY_DEBUG = "debug";
    
    public static final PropertyDescriptor DEBUG = new PropertyDescriptor.Builder()
            .name(KEY_DEBUG)
            .description("Enable debug. If enabled, the original JSON string is embedded in the record_value field of the record.")
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .required(false)
            .defaultValue("false")
            .build();

    @Override
    public void init(final ProcessContext context)
    {
        logger.debug("Initializing Gitlab log Processor");
    }
    
    @Override
    public List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        
        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(DEBUG);

        return Collections.unmodifiableList(descriptors);
    }
  
    @Override
    public Collection<Record> process(ProcessContext context, Collection<Record> records)
    {
        if (debug)
        {
            logger.debug("Gitlab Processor records input: " + records);
        }

        /**
         * Get the original Gitlab log as a JSON string and do some adaptation
         */
        for (Record record : records)
        {
            /**
             *
             * The "conn" first level field states that the event if of type connection.
             */
            String recordValue = (String)record.getField(FieldDictionary.RECORD_VALUE).getRawValue();
            
            // Parse as JSON object
            Map<String, Object> jsonGitlabLog = JsonUtil.convertJsonToMap(recordValue);

            if (jsonGitlabLog.isEmpty())
            {
                logger.error("Empty Gitlab log or error while parsing it: " + record);
                continue;
            }
            
            if (jsonGitlabLog.size() < 1)
            {
                logger.error("Gitlab log should have at least one field: " + record);
                continue;
            }

            // If debug is enabled, we keep the original content of the gitlab log in the record_value field. Otherwise
            // we remove the record_value field.
            if (debug)    
            {
                // Log original JSON string in record_value for debug purpose
                // Clone the map so that even if we change keys in the map, the original key values are kept
                // in the record_value field
                Map<String, Object> normalizedMap = cloneMap(jsonGitlabLog);
                normalizeFields(normalizedMap, null); // Must change '.' characters anyway if want to be able to index in ES  
                record.setField(new Field(FieldDictionary.RECORD_KEY, FieldType.STRING, "gitlab_log_raw"));
                record.setField(new Field(FieldDictionary.RECORD_VALUE, FieldType.MAP, normalizedMap));
            } else
            {
                record.removeField(FieldDictionary.RECORD_KEY);
                record.removeField(FieldDictionary.RECORD_VALUE);
            }

            // Normalize the map key values (Some special characters like '.' are not possible when indexing in ES)
            normalizeFields(jsonGitlabLog, null);
            
            // Set every first level fields of the Gitlab log as first level fields of the record for easier processing
            // in processors following in the current processors stream.
            setGitlabLogFieldsAsFirstLevelFields(jsonGitlabLog, record);
        }

        if (debug)
        {
            logger.debug("Bro Processor records output: " + records);
        }
        return records;
    }
    
    /**
     * Sets the first level fields of the passed Gitlab log as first level fields in the passed Logisland record.
     * @param gitlabLog Gitlab log.
     * @param record Record for which first level fields should be set. 
     */
    private static void setGitlabLogFieldsAsFirstLevelFields(Map<String, Object> gitlabLog, Record record)
    {
        for (Map.Entry<String, Object> jsonEntry : gitlabLog.entrySet())
        {
            String key = jsonEntry.getKey();
            Object value = jsonEntry.getValue();

            if (value instanceof String)
            {
                record.setStringField(key, value.toString());
            } else if (value instanceof Integer)
            {
                record.setField(new Field(key, FieldType.INT, value));
            } else if (value instanceof Long)
            {
                record.setField(new Field(key, FieldType.LONG, value));
            } else if (value instanceof ArrayList)
            {
                record.setField(new Field(key, FieldType.ARRAY, value));
            } else if (value instanceof Float)
            {
                record.setField(new Field(key, FieldType.FLOAT, value));
            } else if (value instanceof Double)
            {
                record.setField(new Field(key, FieldType.DOUBLE, value));
            } else if (value instanceof Map)
            {
                record.setField(new Field(key, FieldType.MAP, value));
            } else if (value instanceof Boolean)
            {
                record.setField(new Field(key, FieldType.BOOLEAN, value));
            } else
            {
                // Unrecognized value type, use string
                record.setStringField(key, JsonUtil.convertToJson(value));
            }
        }
    }

    /**
     * Deeply clones the passed map regarding keys (so that one can modify keys of the original map without changing
     * the clone).
     * @param origMap Map to clone.
     * @return Cloned map.
     */
    private static Map<String, Object> cloneMap(Map<String, Object> origMap)
    {
        Map<String, Object> finalMap = new HashMap<String, Object>();
        origMap.forEach( (key, value) -> {
            if (value instanceof Map)
            {
                Map<String, Object> map = (Map<String, Object>)value;
                finalMap.put(key, (Object)cloneMap(map)); 
            } else
            {
                finalMap.put(key, value);
            }
        });
        return finalMap;
    }
    
    /**
     * Normalize keys in the JSON Gitlab log:
     * - replace any '.' character in the field names with an acceptable character for ES indexing. This must be done up to the highest depth of the event (event may contain
     * sub maps).
     * @param gitlabLog Gitlab log to normalize.
     * @param oldToNewKeys Potential mapping of keys to change into another key (old key -> new key). May be null.
     */
    private static void normalizeFields(Map<String, Object> gitlabLog, Map<String, String> oldToNewKeys)
    {
        List<String> keys = new ArrayList<String>(); // Do not modify the map while iterating over it
        for (String key : gitlabLog.keySet())
        {
            keys.add(key);
        }
        for (String key : keys)
        {
            Object value = gitlabLog.get(key);
            // Is it a key to replace ?
            String newKey = null;
            if ( (oldToNewKeys != null) && oldToNewKeys.containsKey(key) ) // If the oldToNewKeys map is null, do nothing
            {
                newKey = oldToNewKeys.get(key);
            } else
            {
                // Not a special key to replace but we must at least remove unwanted characters
                if (key.contains("."))
                {
                    newKey = key.replaceAll("\\.", "_");
                }
            }            
            
            // Compute new value
            Object newValue = null;
            if (value instanceof Map)
            {
                Map<String, Object> map = (Map<String, Object>)value;
                normalizeFields(map, oldToNewKeys);
                newValue = map;
            } else
            {
                newValue = value;
            }
            
            if (newKey != null)
            {
                gitlabLog.remove(key);
                gitlabLog.put(newKey, newValue);
            }
        }
    }
    
    @Override
    public void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) {

        logger.debug("property {} value changed from {} to {}", descriptor.getName(), oldValue, newValue);
        
        /**
         * Handle the debug property
         */
        if (descriptor.getName().equals(KEY_DEBUG))
        {
          if (newValue != null)
          {
              if (newValue.equalsIgnoreCase("true"))
              {
                  debug = true;
              }
          } else
          {
              debug = false;
          }
        }
    }   
}
