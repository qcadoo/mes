/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.and;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.gt;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;
import static com.qcadoo.model.api.search.SearchRestrictions.idNe;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.dataProviders.AssignmentToShiftCriteria;
import com.qcadoo.mes.assignmentToShift.dataProviders.AssignmentToShiftDataProvider;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangeDescriber;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.shift.ShiftsFactory;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
public class AssignmentToShiftHooks {

    private static final String L_ALREADY_EXISTS = "assignmentToShift.assignmentToShift.entityAlreadyExists";

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private AssignmentToShiftStateChangeDescriber describer;

    @Autowired
    private AssignmentToShiftDataProvider assignmentToShiftDataProvider;

    @Autowired
    private ShiftsFactory shiftsFactory;

    public void onCreate(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        setExternalSynchronized(assignmentToShift);
        setInitialState(assignmentToShift);
    }

    public void onCopy(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        setExternalSynchronized(assignmentToShift);
        setNextDay(assignmentToShift);
        setInitialState(assignmentToShift);
    }

    public boolean onValidate(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        return checkUniqueEntity(assignmentToShift);
    }

    private void setExternalSynchronized(final Entity assignmentToShift) {
        assignmentToShift.setField(AssignmentToShiftFields.EXTERNAL_SYNCHRONIZED, true);
    }

    void setInitialState(final Entity assignmentToShift) {
        stateChangeEntityBuilder.buildInitial(describer, assignmentToShift, AssignmentToShiftState.DRAFT);
    }

    void setNextDay(final Entity assignmentToShift) {
        Optional<LocalDate> maybeNewDate = resolveNextStartDate(assignmentToShift);

        assignmentToShift.setField(AssignmentToShiftFields.START_DATE, maybeNewDate.transform(TO_DATE).orNull());
    }

    private Optional<LocalDate> resolveNextStartDate(final Entity assignmentToShift) {
        LocalDate startDate = LocalDate.fromDateFields(assignmentToShift.getDateField(AssignmentToShiftFields.START_DATE));
        Shift shift = shiftsFactory.buildFrom(assignmentToShift.getBelongsToField(AssignmentToShiftFields.SHIFT));
        Entity factory = assignmentToShift.getBelongsToField(AssignmentToShiftFields.FACTORY);
        Set<LocalDate> occupiedDates = findNextStartDatesMatching(assignmentToShift.getDataDefinition(), startDate, shift,
                factory);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        int daysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
        Iterable<Integer> daysRange = ContiguousSet.create(Range.closed(1, daysInYear), DiscreteDomain.integers());

        return FluentIterable.from(daysRange).transform(startDate::plusDays).firstMatch((final LocalDate localDate) -> !occupiedDates.contains(localDate) && shift.worksAt(localDate));
    }

    private static final Function<Date, LocalDate> TO_LOCAL_DATE = LocalDate::fromDateFields;

    private static final Function<LocalDate, Date> TO_DATE = LocalDate::toDate;

    private Set<LocalDate> findNextStartDatesMatching(final DataDefinition assignmentToShiftDD, final LocalDate laterThan,
            final Shift shift, final Entity factory) {
        AssignmentToShiftCriteria criteria = AssignmentToShiftCriteria.empty();

        criteria.withCriteria(gt(AssignmentToShiftFields.START_DATE, laterThan.toDate()));

        criteria.withShiftCriteria(idEq(shift.getId()));
        criteria.withFactoryCriteria(idEq(factory.getId()));

        List<Entity> matchingStartDatesProjection = assignmentToShiftDataProvider.findAll(criteria,
                Optional.of(alias(field(AssignmentToShiftFields.START_DATE), AssignmentToShiftFields.START_DATE)),
                Optional.of(asc(AssignmentToShiftFields.START_DATE)));

        return FluentIterable.from(matchingStartDatesProjection)
                .transform(EntityUtils.<Date> getFieldExtractor(AssignmentToShiftFields.START_DATE)).transform(TO_LOCAL_DATE)
                .toSet();
    }

    boolean checkUniqueEntity(final Entity assignmentToShift) {
        Date startDate = assignmentToShift.getDateField(AssignmentToShiftFields.START_DATE);
        Entity shift = assignmentToShift.getBelongsToField(AssignmentToShiftFields.SHIFT);
        Entity factory = assignmentToShift.getBelongsToField(AssignmentToShiftFields.FACTORY);
        Entity crew = assignmentToShift.getBelongsToField(AssignmentToShiftFields.CREW);

        AssignmentToShiftCriteria criteria = AssignmentToShiftCriteria.empty();

        SearchCriterion additionalCriteria = eq(AssignmentToShiftFields.START_DATE, startDate);

        criteria.withShiftCriteria(idEq(shift.getId()));
        criteria.withFactoryCriteria(idEq(factory.getId()));
        if (crew != null) {
            criteria.withCrewCriteria(idEq(crew.getId()));
        } else {
            additionalCriteria = and(additionalCriteria, isNull(AssignmentToShiftFields.CREW));
        }

        if (assignmentToShift.getId() != null) {
            additionalCriteria = and(additionalCriteria, idNe(assignmentToShift.getId()));
        }
        criteria.withCriteria(additionalCriteria);

        for (Entity matchingAssignment : assignmentToShiftDataProvider.find(criteria, Optional.of(alias(id(), "id"))).asSet()) {
            addErrorMessages(assignmentToShift.getDataDefinition(), assignmentToShift);

            return false;
        }

        return true;
    }

    private void addErrorMessages(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        assignmentToShift.addError(assignmentToShiftDD.getField(AssignmentToShiftFields.START_DATE), L_ALREADY_EXISTS);
        assignmentToShift.addError(assignmentToShiftDD.getField(AssignmentToShiftFields.SHIFT), L_ALREADY_EXISTS);
        assignmentToShift.addError(assignmentToShiftDD.getField(AssignmentToShiftFields.FACTORY), L_ALREADY_EXISTS);
        assignmentToShift.addError(assignmentToShiftDD.getField(AssignmentToShiftFields.CREW), L_ALREADY_EXISTS);
    }

}
