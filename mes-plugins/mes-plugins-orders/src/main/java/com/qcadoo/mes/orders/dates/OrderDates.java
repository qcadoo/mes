/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.orders.dates;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

public class OrderDates {

    private static final Function<Date, DateTime> TO_DATE_TIME = new Function<Date, DateTime>() {

        @Override
        public DateTime apply(final Date date) {
            return new DateTime(date);
        }
    };

    private final ThreeLevelDate start;

    private final ThreeLevelDate end;

    public static Optional<OrderDates> of(final Entity order) {
        if (hasPlannedDatesDefined(order)) {
            return Optional.of(OrderDates.of(order, Optional.<DateTime> absent(), Optional.<DateTime> absent()));
        } else {
            return Optional.absent();
        }
    }

    private static boolean hasPlannedDatesDefined(final Entity order) {
        return ((order.getField(OrderFields.DATE_FROM) != null) && (order.getField(OrderFields.DATE_TO) != null));
    }

    public static OrderDates of(final Entity order, final DateTime defaultStart, final DateTime defaultEnd) {
        return OrderDates.of(order, Optional.of(defaultStart), Optional.of(defaultEnd));
    }

    private static OrderDates of(final Entity order, final Optional<DateTime> defaultStart, final Optional<DateTime> defaultEnd) {
        ThreeLevelDate startDates = threeLevelDate(order, OrderFields.DATE_FROM, OrderFields.CORRECTED_DATE_FROM,
                OrderFields.EFFECTIVE_DATE_FROM, defaultStart);

        ThreeLevelDate endDates = threeLevelDate(order, OrderFields.DATE_TO, OrderFields.CORRECTED_DATE_TO,
                OrderFields.EFFECTIVE_DATE_TO, defaultEnd);

        return new OrderDates(startDates, endDates);
    }

    public static ThreeLevelDate threeLevelDate(final Entity order, final String plannedDateFieldName,
            final String correctedDateFieldName, final String effectiveDateFieldName, final Optional<DateTime> defaultPlanned) {
        Optional<DateTime> plannedStart = tryExtractLocalDate(order, plannedDateFieldName);

        Preconditions.checkArgument(defaultPlanned.isPresent() || plannedStart.isPresent(),
                "You have to either pass an order with defined planned dates or provide the default values");

        Optional<DateTime> correctedStart = tryExtractLocalDate(order, correctedDateFieldName);
        Optional<DateTime> effectiveStart = tryExtractLocalDate(order, effectiveDateFieldName);

        return new ThreeLevelDate(plannedStart.or(defaultPlanned).get(), correctedStart, effectiveStart);
    }

    private OrderDates(final ThreeLevelDate startDates, final ThreeLevelDate endDates) {
        this.start = startDates;
        this.end = endDates;
    }

    public ThreeLevelDate getStart() {
        return start;
    }

    public ThreeLevelDate getEnd() {
        return end;
    }

    private static Optional<DateTime> tryExtractLocalDate(final Entity order, final String fieldName) {
        return Optional.fromNullable(order.getDateField(fieldName)).transform(TO_DATE_TIME);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }
        OrderDates rhs = (OrderDates) obj;

        return new EqualsBuilder().append(this.start, rhs.start).append(this.end, rhs.end).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(start).append(end).toHashCode();
    }

}
