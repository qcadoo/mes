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
package com.qcadoo.mes.productionPerShift.domain;

import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Date;

public class DailyProgressKey {

    private BigDecimal quantity;

    private Long shiftId;

    private DateTime dateOfDay;

    public DailyProgressKey(Long shiftId, DateTime dateOfDay) {
        this.shiftId = shiftId;
        this.dateOfDay = dateOfDay;
    }

    public DailyProgressKey(Long shiftId, Date dateOfDay) {
        this.shiftId = shiftId;
        this.dateOfDay = new DateTime(dateOfDay);
    }

    public DailyProgressKey(BigDecimal quantity, Long shiftId, DateTime dateOfDay) {
        this.quantity = quantity;
        this.shiftId = shiftId;
        this.dateOfDay = dateOfDay;
    }

    public DailyProgressKey(BigDecimal quantity, Long shiftId, Date dateOfDay) {
        this.quantity = quantity;
        this.shiftId = shiftId;
        this.dateOfDay = new DateTime(dateOfDay);
    }

    public DailyProgressKey from(Entity dailyProgress) {
        return new DailyProgressKey(dailyProgress.getBelongsToField(DailyProgressFields.SHIFT).getId(), dailyProgress
                .getBelongsToField(DailyProgressFields.PROGRESS_FOR_DAY).getDateField(ProgressForDayFields.ACTUAL_DATE_OF_DAY));
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public DateTime getDateOfDay() {
        return dateOfDay;
    }

    public void setDateOfDay(DateTime dateOfDay) {
        this.dateOfDay = dateOfDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DailyProgressKey that = (DailyProgressKey) o;

        if (!dateOfDay.equals(that.dateOfDay))
            return false;
        if (!shiftId.equals(that.shiftId))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = shiftId.hashCode();
        result = 31 * result + dateOfDay.hashCode();
        return result;
    }
}
