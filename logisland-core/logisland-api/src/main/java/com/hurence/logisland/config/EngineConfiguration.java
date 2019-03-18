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

import java.util.ArrayList;
import java.util.List;


public class EngineConfiguration extends AbstractComponentConfiguration {

    private List<StreamConfiguration> streamConfigurations = new ArrayList<>();

    public List<StreamConfiguration> getStreamConfigurations() {
        return streamConfigurations;
    }

    public void addPipelineConfigurations(StreamConfiguration processorChains) {
        this.streamConfigurations.add(processorChains);
    }


    private List<ControllerServiceConfiguration> controllerServiceConfigurations = new ArrayList<>();

    public List<ControllerServiceConfiguration> getControllerServiceConfigurations() {
        return controllerServiceConfigurations;
    }

    public void setControllerServiceConfigurations(List<ControllerServiceConfiguration> controllerServiceConfigurations) {
        this.controllerServiceConfigurations = controllerServiceConfigurations;
    }
}
