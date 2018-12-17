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
package com.hurence.logisland.documentation.init;

import com.hurence.logisland.annotation.lifecycle.OnShutdown;
import com.hurence.logisland.component.ConfigurableComponent;
import com.hurence.logisland.documentation.ConfigurableComponentInitializer;
import com.hurence.logisland.logging.MockComponentLogger;
import com.hurence.logisland.documentation.util.ReflectionUtils;
import com.hurence.logisland.logging.ComponentLog;
import com.hurence.logisland.util.runner.MockProcessContext;
import com.hurence.logisland.processor.Processor;

/**
 * Initializes a Procesor using a MockProcessorInitializationContext
 *
 *
 */
public class ProcessorInitializer implements ConfigurableComponentInitializer {

    @Override
    public void initialize(ConfigurableComponent component) {
        Processor processor = (Processor) component;
      //  processor.init(new MockProcessContext());
    }

    @Override
    public void teardown(ConfigurableComponent component) {
        Processor processor = (Processor) component;


        final ComponentLog logger = new MockComponentLogger(component.getIdentifier() == null ? "" : component.getIdentifier(), component);
        final MockProcessContext context = new MockProcessContext(processor);
        ReflectionUtils.quietlyInvokeMethodsWithAnnotation(OnShutdown.class, processor, logger, context);
    }

}
