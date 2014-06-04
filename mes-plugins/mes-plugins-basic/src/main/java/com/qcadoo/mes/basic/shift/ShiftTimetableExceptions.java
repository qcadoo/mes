package com.qcadoo.mes.basic.shift;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimaps;
import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.basic.constants.ShiftTimetableExceptionFields;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.model.api.Entity;

public class ShiftTimetableExceptions {

    private static final Function<Entity, DateRange> EXTRACT_DATE_RANGE = new Function<Entity, DateRange>() {

        @Override
        public DateRange apply(final Entity timetableExceptionEntity) {
            Date from = timetableExceptionEntity.getDateField(ShiftTimetableExceptionFields.FROM_DATE);
            Date to = timetableExceptionEntity.getDateField(ShiftTimetableExceptionFields.TO_DATE);
            return new DateRange(from, to);
        }
    };

    private static final Function<Entity, TimetableExceptionType> EXTRACT_TYPE = new Function<Entity, TimetableExceptionType>() {

        @Override
        public TimetableExceptionType apply(final Entity timetableExceptionEntity) {
            return TimetableExceptionType
                    .parseString(timetableExceptionEntity.getStringField(ShiftTimetableExceptionFields.TYPE));
        }
    };

    private final ImmutableMultimap<TimetableExceptionType, DateRange> exceptions;

    public ShiftTimetableExceptions(final Entity shift) {
        this.exceptions = getExceptions(shift);
    }

    private ImmutableMultimap<TimetableExceptionType, DateRange> getExceptions(final Entity shift) {
        List<Entity> timetableExceptionEntities = shift.getHasManyField(ShiftFields.TIMETABLE_EXCEPTIONS);
        return ImmutableMultimap.copyOf(Multimaps.transformValues(Multimaps.index(timetableExceptionEntities, EXTRACT_TYPE),
                EXTRACT_DATE_RANGE));
    }

    public boolean hasFreeTimeAt(final Date date) {
        return findDateRangeFor(TimetableExceptionType.FREE_TIME, date) != null;
    }

    public boolean hasWorkTimeAt(final Date date) {
        return findDateRangeFor(TimetableExceptionType.WORK_TIME, date) != null;
    }

    public DateRange findDateRangeFor(final TimetableExceptionType type, final Date date) {
        // TODO MAKU optimize if needed (sorted dates + break if subsequent date range doesn't start after given date?).
        // But for now this might be an overhead so I'm leaving it out.
        for (DateRange dateRange : exceptions.get(type)) {
            if (dateRange.contains(date)) {
                return dateRange;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return exceptions.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ShiftTimetableExceptions)) {
            return false;
        }

        ShiftTimetableExceptions other = (ShiftTimetableExceptions) obj;

        return ObjectUtils.equals(exceptions, other.exceptions);
    }

}
