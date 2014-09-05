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
package com.qcadoo.mes.deviationCausesReporting;

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;

public final class DeviationsReportCriteria {

    private static final ImmutableSet<String> EXCLUDED_ORDER_STATES = ImmutableSet.of(OrderStateStringValues.PENDING);

    private final Interval searchInterval;

    public static DateTime getDefaultDateTo() {
        return DateTime.now().withTimeAtStartOfDay().plusDays(1).minusMillis(1);
    }

    public static DeviationsReportCriteria forDates(final LocalDate fromDate, final Optional<LocalDate> maybeToDate) {
        checkArguments(fromDate, maybeToDate);
        return fromLocalDates(fromDate, maybeToDate);
    }

    public static DeviationsReportCriteria forDates(final DateTime fromDate, final Optional<DateTime> maybeToDate) {
        checkArguments(fromDate, maybeToDate);
        LocalDate fromLocalDate = fromDate.toLocalDate();
        Optional<LocalDate> maybeToLocalDate = maybeToDate.transform(new Function<DateTime, LocalDate>() {

            @Override
            public LocalDate apply(final DateTime input) {
                return input.toLocalDate();
            }
        });
        return fromLocalDates(fromLocalDate, maybeToLocalDate);
    }

    private static DeviationsReportCriteria fromLocalDates(final LocalDate fromDate, final Optional<LocalDate> maybeToDate) {
        DateTime rangeBegin = fromDate.toDateTimeAtStartOfDay();
        DateTime rangeEnd = maybeToDate.or(LocalDate.now()).plusDays(1).toDateTimeAtStartOfDay().minusMillis(1);
        Preconditions.checkArgument(rangeBegin.isBefore(rangeEnd), "Passed dates have wrong order.");
        Interval searchInterval = new Interval(rangeBegin, rangeEnd);
        return new DeviationsReportCriteria(searchInterval);
    }

    private static void checkArguments(final Object fromDate, final Optional<?> maybeToDate) {
        Preconditions.checkArgument(fromDate != null, "Lower bound for dates' search interval is mandatory!");
        Preconditions.checkArgument(maybeToDate != null,
                "Upper bound for dates' search interval is not mandatory, but you have to represent it's absence "
                        + "explicitly, by passing Optional.absent() instead of null!");
    }

    private DeviationsReportCriteria(final Interval searchInterval) {
        this.searchInterval = searchInterval;
    }

    public Interval getSearchInterval() {
        return searchInterval;
    }

    public ImmutableSet<String> getExcludedOrderStates() {
        return EXCLUDED_ORDER_STATES;
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
        DeviationsReportCriteria rhs = (DeviationsReportCriteria) obj;
        return ObjectUtils.equals(this.searchInterval, rhs.searchInterval);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(searchInterval);
    }
}
