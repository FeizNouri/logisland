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
package com.caseystella.analytics.distribution;

import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Created by cstella on 2/28/16.
 */
public enum RangeApproximator implements Function<Range<Double>, Double> {
    MIDPOINT(new Function<Range<Double>, Double>() {
        @Nullable
        @Override
        public Double apply(@Nullable Range<Double> doubleRange) {
            return (doubleRange.getEnd() + doubleRange.getBegin())/2;
        }
    })
    ;
    private Function<Range<Double>, Double> _func;
    RangeApproximator(Function<Range<Double>, Double> func) {
        _func = func;
    }
    @Nullable
    @Override
    public Double apply(@Nullable Range<Double> doubleRange) {
        return _func.apply(doubleRange);
    }
}
