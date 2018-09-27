/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.timeGapsPreview;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.Duration;
import org.joda.time.Interval;

/**
 * Immutable tim gaps searching context
 */
public class TimeGapsContext {

    private final Interval interval;

    private final Set<Long> productionLines;

    private final Duration duration;

    public TimeGapsContext(final Interval interval, final Set<Long> productionLinesDomain, final Duration durationFilter) {
        this.interval = interval;
        this.productionLines = Collections.unmodifiableSet(productionLinesDomain);
        this.duration = durationFilter;
    }

    public Interval getInterval() {
        return interval;
    }

    public Set<Long> getProductionLines() {
        return productionLines;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TimeGapsContext that = (TimeGapsContext) o;
        return new EqualsBuilder().append(interval, that.interval).append(productionLines, that.productionLines)
                .append(duration, that.duration).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(interval).append(productionLines).append(duration).toHashCode();
    }
}
