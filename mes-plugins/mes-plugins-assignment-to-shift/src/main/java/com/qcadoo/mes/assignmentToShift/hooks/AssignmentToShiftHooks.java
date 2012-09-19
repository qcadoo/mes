/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.START_DATE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STATE;
import static com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState.DRAFT;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangeDescriber;
import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class AssignmentToShiftHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private AssignmentToShiftStateChangeDescriber describer;

    @Autowired
    private ShiftsServiceImpl shiftService;

    public void setInitialState(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        stateChangeEntityBuilder.buildInitial(describer, assignmentToShift, DRAFT);
    }

    public void clearState(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        assignmentToShift.setField(STATE, DRAFT.getStringValue());
    }

    public void setNextDay(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {

        Date newDate;
        Date currentDate = (Date) assignmentToShift.getField("startDate");
        int i = 1;

        do {
            newDate = new DateTime(currentDate).plusDays(i).toDate();
            i++;
        } while (!shiftService.checkIfShiftWorkAtDate(newDate, assignmentToShift.getBelongsToField(SHIFT))
                || !searchResultOfAssignmentToShift(assignmentToShiftDD, assignmentToShift, newDate));

        assignmentToShift.setField("startDate",
                new SimpleDateFormat(DateUtils.L_DATE_FORMAT, LocaleContextHolder.getLocale()).format(newDate));

    }

    final private boolean searchResultOfAssignmentToShift(final DataDefinition assignmentToShiftDD,
            final Entity assignmentToShift, final Object startDate) {

        SearchResult searchResult = assignmentToShiftDD.find()
                .add(SearchRestrictions.belongsTo(SHIFT, assignmentToShift.getBelongsToField(SHIFT)))
                .add(SearchRestrictions.eq(START_DATE, startDate)).list();

        if (searchResult.getEntities().isEmpty()) {
            return true;
        } else {
            return false;
        }

    }

    public boolean checkUniqueEntity(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        SearchCriteriaBuilder searchCriteriaBuilder = assignmentToShiftDD.find()
                .add(SearchRestrictions.belongsTo(SHIFT, assignmentToShift.getBelongsToField(SHIFT)))
                .add(SearchRestrictions.eq(START_DATE, assignmentToShift.getField(START_DATE)));

        if (assignmentToShift.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", assignmentToShift.getId()));
        }

        Entity existingAssignmentToShift = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (existingAssignmentToShift != null) {
            assignmentToShift.addError(assignmentToShiftDD.getField(SHIFT),
                    "assignmentToShift.assignmentToShift.entityAlreadyExists");
            assignmentToShift.addError(assignmentToShiftDD.getField(START_DATE),
                    "assignmentToShift.assignmentToShift.entityAlreadyExists");

            return false;
        }

        return true;
    }

}
