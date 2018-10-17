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
package com.hurence.logisland.processor;

import com.hurence.logisland.annotation.documentation.CapabilityDescription;
import com.hurence.logisland.annotation.documentation.Tags;
import com.hurence.logisland.component.PropertyDescriptor;
import com.hurence.logisland.record.Field;
import com.hurence.logisland.record.FieldDictionary;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.util.string.JsonUtil;
import com.hurence.logisland.validator.StandardValidators;
import com.hurence.logisland.validator.ValidationContext;
import com.hurence.logisland.validator.ValidationResult;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.mail.resolver.DataSourceClassPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Tags({"json"})
@CapabilityDescription(
        "The SetFlatJsonAsAttributes processor reads the content of a string field containing a simple flat json " +
                "string and sets each json attribute as a field of the current record. A flat json is a json " +
                "document containing only first level attributes i.e: {\"attribute1\": \"simpleValue1\", " +
                "\"attribute2\": \"simpleValue2\"}. That is, the value of a first level json attribute is only a " +
                "simple type, not an object nor an array. Note that this could be achieved with the EvaluateJsonPath " +
                "attributes, but this implies to declare each json first level attribute in the configuration " +
                "and also to know by advance every one of them. Whereas for this simple case, " +
                "the SetFlatJsonAsAttributes processor does not require such a configuration and will work with any " +
                "incoming flat json, regardless of the list of first level attributes.")
public class SetFlatJsonAsAttributes extends AbstractProcessor {

    private static Logger logger = LoggerFactory.getLogger(SetFlatJsonAsAttributes.class);

    // Easy trick to not allow debugging without changing the logger level but instead using a configuration key
    private boolean debug = false;

    private String jsonField = FieldDictionary.RECORD_VALUE;
    private boolean keepJsonField = false;
    private boolean overwriteExistingField = true;

    // Easy trick to not allow debugging without changing the logger level but instead using a configuration key
    private static final String KEY_DEBUG = "debug";
    private static final String KEY_JSON_FIELD = "json.field";
    private static final String KEY_KEEP_JSON_FIELD = "keep.json.field";
    private static final String KEY_OVERWRITE_EXISTING_FIELD = "overwrite.existing.field";
    
    public static final PropertyDescriptor DEBUG = new PropertyDescriptor.Builder()
            .name(KEY_DEBUG)
            .description("Enable debug. If enabled, debug information are written to stdout.")
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .required(false)
            .defaultValue("false")
            .build();
    
    public static final PropertyDescriptor JSON_FIELD = new PropertyDescriptor.Builder()
            .name(KEY_JSON_FIELD)
            .description("Field name of the string field that contains the json document to parse.")
            .required(true)
            .defaultValue(FieldDictionary.RECORD_VALUE)
            .build();

    public static final PropertyDescriptor KEEP_JSON_FIELD = new PropertyDescriptor.Builder()
            .name(KEY_KEEP_JSON_FIELD)
            .description("Keep the original json field or not. Default is false so default is to remove the json field.")
            .required(true)
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .defaultValue("false")
            .build();

    public static final PropertyDescriptor OVERWRITE_EXISTING_FIELD = new PropertyDescriptor.Builder()
            .name(KEY_OVERWRITE_EXISTING_FIELD)
            .description("Overwrite an existing record field or not. Default is true so default is to remove the " +
                    "conflicting field.")
            .required(true)
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .defaultValue("true")
            .build();

    @Override
    public List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        
        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(DEBUG);
        descriptors.add(JSON_FIELD);
        descriptors.add(KEEP_JSON_FIELD);
        descriptors.add(OVERWRITE_EXISTING_FIELD);

        return Collections.unmodifiableList(descriptors);
    }

    @Override
    public void init(final ProcessContext context)
    {
        logger.debug("Initializing SetFlatJsonAsAttributes Processor");
    }
  
    @Override
    public Collection<Record> process(ProcessContext context, Collection<Record> records)
    {
        if (debug)
        {
            logger.info("SetFlatJsonAsAttributes Processor records input: " + records);
        }

        /**
         * Transform the records into mails and send them
         */
        for (Record record : records)
        {            
            String jsonString = getStringField(record, jsonField);
            if (jsonString == null)
            {
                // No json content, ignore this record
                continue;
            }

            // Parse as JSON object
            Map<String, Object> json = JsonUtil.convertJsonToMap(jsonString);

            // Set json attribute as record fields
            setJsonFirstLevelAttributesAsFirstLevelFields(json, record);

            // Delete original json field if requested
            if (!keepJsonField)
            {
                record.removeField(jsonField);
            }
        }

        if (debug)
        {
            logger.info("SetFlatJsonAsAttributes Processor records output: " + records);
        }
        return records;
    }
    
    /**
     * Retrieve the record field value
     * @param fieldName The name of the string field
     * @return The value of the field or null if the field is not present in the record
     */
    private String getStringField(Record record, String fieldName)
    {
        Field field = record.getField(fieldName);
        if (field != null)
        {
            return field.asString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Sets the first level attributes of the passed json object as first level fields in the passed Logisland record.
     * @param json json string.
     * @param record Record for which first level fields should be set.
     */
    private void setJsonFirstLevelAttributesAsFirstLevelFields(Map<String, Object> json, Record record)
    {
        for (Map.Entry<String, Object> jsonEntry : json.entrySet())
        {
            String key = jsonEntry.getKey();
            Object value = jsonEntry.getValue();

            if (!overwriteExistingField)
            {
                Field existingField = record.getField(key);
                if (existingField != null) {
                    // Skip conflicting existing field
                    continue;
                }
            }

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

    @Override
    public void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) {

        logger.debug("property {} value changed from {} to {}", descriptor.getName(), oldValue, newValue);
        
        /**
         * Handle the DEBUG property
         */
        if (descriptor.equals(DEBUG))
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
        
        /**
         * Handle the JSON_FIELD property
         */
        if (descriptor.equals(JSON_FIELD))
        {
            jsonField = newValue;
        }

        /**
         * Handle the KEEP_JSON_FIELD property
         */
        if (descriptor.equals(KEEP_JSON_FIELD))
        {
            if (newValue != null)
            {
                if (newValue.equalsIgnoreCase("true"))
                {
                    keepJsonField = true;
                }
            } else
            {
                keepJsonField = false;
            }
        }

        /**
         * Handle the OVERWRITE_EXISTING_FIELD property
         */
        if (descriptor.equals(OVERWRITE_EXISTING_FIELD))
        {
            if (newValue != null)
            {
                if (newValue.equalsIgnoreCase("true"))
                {
                    overwriteExistingField = true;
                } else
                {
                    overwriteExistingField = false;
                }
            } else
            {
                overwriteExistingField = true;
            }
        }
        
        if (debug)
        {
            displayConfig();
        }
    }

    /**
     * Displays processor configuration
     */
    private void displayConfig()
    {
        StringBuilder sb = new StringBuilder("SetFlatJsonAsAttributes Processor configuration:");
        sb.append("\n" + JSON_FIELD.getName() + ": " + jsonField);
        sb.append("\n" + KEEP_JSON_FIELD.getName() + ": " + keepJsonField);
        sb.append("\n" + OVERWRITE_EXISTING_FIELD.getName() + ": " + overwriteExistingField);
        logger.info(sb.toString());
    }
}
