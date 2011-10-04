package com.qcadoo.mes.basic;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalTime;

import com.qcadoo.mes.basic.ShiftsServiceImpl.ShiftHour;
import com.qcadoo.model.api.Entity;

public interface ShiftsService {

    public LocalTime[][] convertDayHoursToInt(final String string);

    public List<ShiftHour> getHoursForAllShifts(final Date dateFrom, final Date dateTo);

    public Date findDateFromForOrder(final Date dateTo, final long seconds);

    public Date findDateToForOrder(final Date dateFrom, final long seconds);

    public List<ShiftHour> getHoursForShift(final Entity shift, final Date dateFrom, final Date dateTo);
}
