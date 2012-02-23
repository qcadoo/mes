/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.basic;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalTime;

import com.qcadoo.mes.basic.ShiftsServiceImpl.ShiftHour;
import com.qcadoo.model.api.Entity;

public interface ShiftsService {

    LocalTime[][] convertDayHoursToInt(final String string);

    List<ShiftHour> getHoursForAllShifts(final Date dateFrom, final Date dateTo);

    Date findDateFromForOrder(final Date dateTo, final long seconds);

    Date findDateToForOrder(final Date dateFrom, final long seconds);

    List<ShiftHour> getHoursForShift(final Entity shift, final Date dateFrom, final Date dateTo);

    Entity getShiftFromDate(final Date date);
}
