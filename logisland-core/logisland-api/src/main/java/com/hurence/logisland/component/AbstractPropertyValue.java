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
package com.hurence.logisland.component;

import com.hurence.logisland.controller.ControllerService;
import com.hurence.logisland.controller.ControllerServiceLookup;
import com.hurence.logisland.record.FieldDictionary;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import com.hurence.logisland.registry.VariableRegistry;
import com.hurence.logisland.util.FormatUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by mathieu on 08/06/17.
 */
public abstract class AbstractPropertyValue implements PropertyValue {

    protected Object rawValue;
    protected ControllerServiceLookup serviceLookup;
    protected VariableRegistry variableRegistry;

    @Override
    public String getRawValue() {
        if (rawValue == null) {
            return null;
        }

        return rawValue.toString();
    }

    @Override
    public String asString() {
        return getRawValue();
    }

    @Override
    public Integer asInteger() {
        return (getRawValue() == null) ? null : Integer.parseInt(getRawValue().trim());
    }

    @Override
    public Long asLong() {
        return (getRawValue() == null) ? null : Long.parseLong(getRawValue().trim());
    }

    @Override
    public Boolean asBoolean() {
        return (getRawValue() == null) ? null : Boolean.parseBoolean(getRawValue().trim());
    }

    @Override
    public Float asFloat() {
        return (getRawValue() == null) ? null : Float.parseFloat(getRawValue().trim());
    }

    @Override
    public Double asDouble() {
        return (getRawValue() == null) ? null : Double.parseDouble(getRawValue().trim());
    }

    @Override
    public Long asTimePeriod(final TimeUnit timeUnit) {
        return (rawValue == null) ? null : FormatUtils.getTimeDuration(rawValue.toString().trim(), timeUnit);
    }


    @Override
    public boolean isSet() {
        return getRawValue() != null;
    }

    @Override
    public Record asRecord() {
        return (getRawValue() == null) ? null : new StandardRecord()
                .setStringField(FieldDictionary.RECORD_VALUE,getRawValue().trim());
    }

    @Override
    public ControllerService asControllerService() {
        if (getRawValue() == null || getRawValue().equals("") || serviceLookup == null) {
            return null;
        }

        return serviceLookup.getControllerService(getRawValue());
    }

    
    @Override
    public PropertyValue evaluate(Record record) {
        // does nothing
        return this;
    }
}
