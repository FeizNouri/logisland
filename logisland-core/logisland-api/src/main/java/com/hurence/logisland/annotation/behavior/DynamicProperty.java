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
package com.hurence.logisland.annotation.behavior;



import com.hurence.logisland.component.ConfigurableComponent;

import java.lang.annotation.*;

/**
 * An annotation that may be placed on a {@link ConfigurableComponent} to
 * indicate that it supports a dynamic property.
 *
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DynamicProperty {

    String name();

    boolean supportsExpressionLanguage() default false;

    String value();

    String description();

    /**
     *
     * @return the property name to use for generating documentation (to get the corresponding dynamic property sample)
     */
    String nameForDoc() default "fakePropertyThatDoesNotExistSoThatWeGetADynamicPropertySampleToGenerateTheDocumentation";
}
