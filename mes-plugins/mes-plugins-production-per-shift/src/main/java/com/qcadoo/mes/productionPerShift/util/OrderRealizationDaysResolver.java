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
package com.qcadoo.mes.productionPerShift.util;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class OrderRealizationDaysResolver {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public OrderRealizationDayWithShifts find(final DateTime orderStartDate, final int startingFrom, final boolean isFirstDay) {
        OrderRealizationDayWithShifts firstWorkingDay = findFirstWorkingDayAfter(orderStartDate, startingFrom);

        Shift shiftStartingOrder = firstWorkingDay.findShiftWorkingAt(orderStartDate.toLocalTime());
        if (isFirstDay && startingShiftIsWorkingFromPreviousDay(shiftStartingOrder, orderStartDate)) {
            return new OrderRealizationDayWithShifts(orderStartDate, firstWorkingDay.getDaysAfterStartDate() - 1,
                    Lists.newArrayList(shiftStartingOrder));
        }
        return firstWorkingDay;
    }

    private boolean startingShiftIsWorkingFromPreviousDay(final Shift shift, final DateTime orderStartDate) {
        LocalTime time = orderStartDate.toLocalTime();
        if (shift == null) {
            return false;
        }
        TimeRange foundTimeRange = shift.findWorkTimeAt(orderStartDate.getDayOfWeek(), time);
        return foundTimeRange != null && foundTimeRange.startsDayBefore();
    }

    private static final int DAYS_IN_YEAR = 365;

    private OrderRealizationDayWithShifts findFirstWorkingDayAfter(final DateTime orderStartDate, final int startFrom) {
        for (int offset = startFrom; offset < startFrom + DAYS_IN_YEAR; offset++) {
            List<Shift> workingShifts = getShiftsWorkingAt(orderStartDate.plusDays(offset).getDayOfWeek());
            if (!workingShifts.isEmpty()) {
                return new OrderRealizationDayWithShifts(orderStartDate, offset, workingShifts);
            }
        }
        // assuming that no working shifts in period of year length means that client does not use shifts calendar and there won't
        // be any offsets caused by work free time boundaries.
        return new OrderRealizationDayWithShifts(orderStartDate, startFrom + 1, Collections.<Shift> emptyList());
    }

    private List<Shift> getShiftsWorkingAt(final int dayOfWeek) {
        List<Shift> matchingShifts = Lists.newArrayList();
        for (Entity shiftEntity : getAllShifts()) {
            Shift shift = new Shift(shiftEntity);
            if (shift.worksAt(dayOfWeek)) {
                matchingShifts.add(shift);
            }
        }
        return matchingShifts;
    }

    private List<Entity> getAllShifts() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT).find().list()
                .getEntities();
    }

    public static final class OrderRealizationDayWithShifts {

        private final List<Shift> workingShifts;

        private final int daysAfterStartDate;

        private final DateTime orderStartDate;

        private OrderRealizationDayWithShifts(final DateTime orderStartDate, final int day, final Iterable<Shift> shifts) {
            this.orderStartDate = orderStartDate;
            this.daysAfterStartDate = day;
            this.workingShifts = Lists.newArrayList(shifts);
        }

        public Shift findShiftWorkingAt(final LocalTime time) {
            int dayOfWeek = orderStartDate.plusDays(daysAfterStartDate).getDayOfWeek();
            for (Shift shift : workingShifts) {
                if (shift.worksAt(dayOfWeek, time)) {
                    return shift;
                }
            }
            return null;
        }

        public boolean isEmpty() {
            return workingShifts.isEmpty();
        }

        public List<Shift> getWorkingShifts() {
            return workingShifts;
        }

        public int getDaysAfterStartDate() {
            return daysAfterStartDate;
        }

        public DateTime getDateTime() {
            return orderStartDate.plusDays(daysAfterStartDate);
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(daysAfterStartDate).append(orderStartDate).append(workingShifts).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof OrderRealizationDayWithShifts)) {
                return false;
            }

            OrderRealizationDayWithShifts other = (OrderRealizationDayWithShifts) obj;

            return new EqualsBuilder().append(daysAfterStartDate, other.daysAfterStartDate)
                    .append(orderStartDate, other.orderStartDate).append(workingShifts, other.workingShifts).isEquals();
        }

    }

}
