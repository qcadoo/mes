package com.qcadoo.mes.productionPerShift.dates;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.commons.functional.LazyStream;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.shift.ShiftsDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.dates.OrderDates;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
public class ProgressDatesService {

    private static final Function<Entity, Long> EXTRACT_TECHNOLOGY_OPERATION_ID = new Function<Entity, Long>() {

        @Override
        public Long apply(final Entity progressForDay) {
            return FluentOptional
                    .fromNullable(progressForDay.getBelongsToField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT))
                    .flatMap(EntityUtils.getSafeIdExtractor()).or(0L);
        }
    };

    @Autowired
    private ProgressForDayDataProvider progressForDayDataProvider;

    @Autowired
    private OrderRealizationDaysResolver orderRealizationDaysResolver;

    @Autowired
    private ShiftsDataProvider shiftsDataProvider;

    @Transactional
    public void setUpDatesFor(final Entity order) {
        for (OrderDates orderDates : resolveOrderDates(order).asSet()) {
            List<Entity> progressesForDays = progressForDayDataProvider.findForOrder(order,
                    ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER);
            Multimap<Long, Entity> progressesByOperationId = Multimaps.index(progressesForDays, EXTRACT_TECHNOLOGY_OPERATION_ID);
            for (Collection<Entity> progresses : progressesByOperationId.asMap().values()) {
                setupDatesFor(progresses, orderDates);
            }
        }
    }

    private Optional<OrderDates> resolveOrderDates(final Entity order) {
        Date plannedStart = order.getDateField(OrderFields.DATE_FROM);
        if (plannedStart == null) {
            return Optional.absent();
        }
        // Order realization end time is not required, thus I've passed some arbitrary values here.
        return Optional.of(OrderDates.of(order, new DateTime(plannedStart), new DateTime(plannedStart).plusWeeks(1)));
    }

    private void setupDatesFor(final Collection<Entity> progressesForDays, final OrderDates orderDates) {
        ImmutableMap<ProgressType, RealizationDays> realizationDays = resolveRealizationDaysFor(orderDates, progressesForDays);
        for (Entity progressForDay : progressesForDays) {
            DayAndDates dayAndDates = realizationDays.get(ProgressType.of(progressForDay)).getDatesFor(progressForDay);
            setUpProgressForDayEntity(progressForDay, dayAndDates);
            progressForDay.getDataDefinition().save(progressForDay);
        }
    }

    public ImmutableMap<ProgressType, RealizationDays> resolveRealizationDaysFor(final OrderDates orderDates,
            final Collection<Entity> progressesForDays) {
        List<Shift> shifts = shiftsDataProvider.findAll();
        return ImmutableMap.<ProgressType, RealizationDays> builder()
                .put(ProgressType.PLANNED, new RealizationDays(progressesForDays, ProgressType.PLANNED, orderDates, shifts))
                .put(ProgressType.CORRECTED, new RealizationDays(progressesForDays, ProgressType.CORRECTED, orderDates, shifts))
                .build();
    }

    private void setUpProgressForDayEntity(final Entity progressForDay, final DayAndDates realizationDayAndDates) {
        progressForDay.setField(ProgressForDayFields.DAY, realizationDayAndDates.getDay());
        progressForDay.setField(ProgressForDayFields.DATE_OF_DAY, realizationDayAndDates.getDate().toDate());
        progressForDay.setField(ProgressForDayFields.ACTUAL_DATE_OF_DAY, realizationDayAndDates.getEffectiveDate().toDate());
    }

    /* mutable :( */
    private class RealizationDays {

        /* mutated by removeAvailableDates */
        private final NavigableMap<Integer, LocalDate> daysToDates;

        /* mutated by removeAvailableDates */
        private final NavigableMap<Integer, LocalDate> daysToEffectiveDates;

        private RealizationDays(final Collection<Entity> progressesForDays, final ProgressType progressType,
                final OrderDates orderDates, final List<Shift> shifts) {
            Collection<Entity> progressesOfGivenType = Collections2.filter(progressesForDays, new Predicate<Entity>() {

                @Override
                public boolean apply(final Entity input) {
                    return ProgressType.of(input) == progressType;
                }
            });
            DateTime startDate = progressType.extractStartDateTimeFrom(orderDates);
            this.daysToDates = buildDayNumToDateMap(startDate, progressesOfGivenType, shifts);

            DateTime effectiveStartDate = orderDates.getStart().effectiveWithFallback();
            this.daysToEffectiveDates = buildDayNumToDateMap(effectiveStartDate, progressesForDays, shifts);
        }

        private NavigableMap<Integer, LocalDate> buildDayNumToDateMap(final DateTime startDateTime,
                final Collection<Entity> progressesForDays, final List<Shift> shifts) {
            int maxDayNum = max(EntityUtils.<Integer> getFieldsView(progressesForDays, ProgressForDayFields.DAY));
            LazyStream<OrderRealizationDay> plannedStream = orderRealizationDaysResolver.asStreamFrom(startDateTime, shifts);
            Map<Integer, OrderRealizationDay> daysToRealizationDay = Maps
                    .uniqueIndex(Iterables.limit(plannedStream, maxDayNum + 1 + progressesForDays.size()),
                            OrderRealizationDay.EXTRACT_DAY_NUM);
            Map<Integer, LocalDate> daysToDate = Maps.transformValues(daysToRealizationDay, OrderRealizationDay.EXTRACT_DATE);
            return new TreeMap<Integer, LocalDate>(daysToDate);
        }

        private Integer max(final Collection<Integer> values) {
            if (values.isEmpty()) {
                return 0;
            }
            return Ordering.natural().max(values);
        }

        public DayAndDates getDatesFor(final Entity progressForDay) {
            int originalDayNumber = progressForDay.getIntegerField(ProgressForDayFields.DAY);
            Map.Entry<Integer, LocalDate> dayAndDate = daysToDates.ceilingEntry(originalDayNumber);
            LocalDate effectiveDate = daysToEffectiveDates.ceilingEntry(originalDayNumber).getValue();
            /* mutates state */
            removeAvailableDates(originalDayNumber);
            return new DayAndDates(dayAndDate.getKey(), dayAndDate.getValue(), effectiveDate);
        }

        /* mutates state */
        private void removeAvailableDates(final int dayNumber) {
            for (Integer day : Optional.fromNullable(daysToDates.ceilingKey(dayNumber)).asSet()) {
                daysToDates.remove(day);
            }
            for (Integer day : Optional.fromNullable(daysToEffectiveDates.ceilingKey(dayNumber)).asSet()) {
                daysToEffectiveDates.remove(day);
            }
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
            RealizationDays rhs = (RealizationDays) obj;
            return new EqualsBuilder().append(this.daysToDates, rhs.daysToDates)
                    .append(this.daysToEffectiveDates, rhs.daysToEffectiveDates).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(daysToDates).append(daysToEffectiveDates).toHashCode();
        }
    }

    /* immutable */
    static class DayAndDates {

        private final int day;

        private final LocalDate date;

        private final LocalDate effectiveDate;

        DayAndDates(final int day, final LocalDate date, final LocalDate effectiveDate) {
            this.day = day;
            this.date = date;
            this.effectiveDate = effectiveDate;
        }

        public int getDay() {
            return day;
        }

        public LocalDate getDate() {
            return date;
        }

        public LocalDate getEffectiveDate() {
            return effectiveDate;
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
            DayAndDates rhs = (DayAndDates) obj;
            return new EqualsBuilder().append(this.day, rhs.day).append(this.date, rhs.date)
                    .append(this.effectiveDate, rhs.effectiveDate).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(day).append(date).append(effectiveDate).toHashCode();
        }
    }

}
