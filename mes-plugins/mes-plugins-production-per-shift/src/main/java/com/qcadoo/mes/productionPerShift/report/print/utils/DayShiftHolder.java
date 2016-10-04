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
package com.qcadoo.mes.productionPerShift.report.print.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.joda.time.DateTime;

import com.qcadoo.model.api.Entity;

public class DayShiftHolder {

    private Entity shift;

    private DateTime day;

    private HSSFCell cell;


    public DayShiftHolder(Entity shift, DateTime day, HSSFCell cell) {
        this.shift = shift;
        this.day = day;
        this.cell = cell;
    }

    public Entity getShift() {
        return shift;
    }

    public void setShift(Entity shift) {
        this.shift = shift;
    }

    public DateTime getDay() {
        return day;
    }

    public void setDay(DateTime day) {
        this.day = day;
    }

    public HSSFCell getCell() {
        return cell;
    }

    public void setCell(HSSFCell cell) {
        this.cell = cell;
    }
}
