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
package com.qcadoo.mes.assignmentToShift.print.xls;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.START_DATE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STATE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_FROM;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_TO;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.PRODUCTION_LINE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.WORKER;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState.ACCEPTED;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState.CORRECTED;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AssignmentToShiftXlsHelper {

    private static final String EMPTY = "";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    public List<DateTime> getDaysBetweenGivenDates(final Entity entity) {
        List<DateTime> days = new LinkedList<DateTime>();

        DateTime dateFrom = new DateTime((Date) entity.getField(DATE_FROM));
        DateTime dateTo = new DateTime((Date) entity.getField(DATE_TO));

        DateTime nextDay = dateFrom;

        int numberOfDays = Days.daysBetween(dateFrom.toDateMidnight(), dateTo.toDateMidnight()).getDays();

        days.add(nextDay);

        int oneDay = 1;
        while (numberOfDays != 0) {
            nextDay = nextDay.plusDays(oneDay).toDateTime();
            days.add(nextDay);
            numberOfDays--;
        }

        return days;
    }

    public int getNumberOfDaysBetweenGivenDates(final Entity entity) {
        DateTime dateFrom = new DateTime((Date) entity.getField(DATE_FROM));
        DateTime dateTo = new DateTime((Date) entity.getField(DATE_TO));

        return Days.daysBetween(dateFrom.toDateMidnight(), dateTo.toDateMidnight()).getDays();
    }

    public List<Entity> getAssignmentToShift(final Entity entity) {
        return dataDefinitionService
                .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT).find()
                .add(SearchRestrictions.belongsTo(SHIFT, entity.getBelongsToField(SHIFT))).list().getEntities();
    }

    public Entity getAssignmentToShift(final Entity shift, final Date date) {
        boolean shiftWorks = shiftsService.checkIfShiftWorkAtDate(date, shift);
        if (shiftWorks) {
            return dataDefinitionService
                    .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT)
                    .find()
                    .add(SearchRestrictions.belongsTo(SHIFT, shift))
                    .add(SearchRestrictions.or(SearchRestrictions.eq(AssignmentToShiftFields.STATE,
                            AssignmentToShiftState.ACCEPTED.getStringValue()), SearchRestrictions.eq(
                            AssignmentToShiftFields.STATE, AssignmentToShiftState.CORRECTED.getStringValue())))
                    .add(SearchRestrictions.le(START_DATE, date)).addOrder(SearchOrders.desc(START_DATE)).setMaxResults(1)
                    .uniqueResult();
        } else {
            return null;
        }
    }

    public String getListOfWorkers(final List<Entity> staffAssignmentToShifts) {
        if (staffAssignmentToShifts == null) {
            return EMPTY;
        }

        StringBuilder listOfWorkers = new StringBuilder();

        for (Entity staffAssignmentToShift : staffAssignmentToShifts) {
            Entity worker = staffAssignmentToShift.getBelongsToField(WORKER);
            listOfWorkers.append(worker.getStringField("name"));
            listOfWorkers.append(" ");
            listOfWorkers.append(worker.getStringField("surname"));

            if (staffAssignmentToShifts.indexOf(staffAssignmentToShift) != (staffAssignmentToShifts.size() - 1)) {
                listOfWorkers.append(", ");
            }
        }

        return listOfWorkers.toString();
    }

    public List<String> getListOfWorker(final List<Entity> staffAssignmentToShifts) {
        List<String> listOfWorkers = Lists.newArrayList();
        if (staffAssignmentToShifts == null) {
            return listOfWorkers;
        }

        for (Entity staffAssignmentToShift : staffAssignmentToShifts) {
            Entity workerEntity = staffAssignmentToShift.getBelongsToField(WORKER);
            StringBuilder worker = new StringBuilder();
            worker.append(workerEntity.getStringField("name"));
            worker.append(" ");
            worker.append(workerEntity.getStringField("surname"));
            listOfWorkers.add(worker.toString());

        }

        return listOfWorkers;
    }

    public String getListOfWorkersWithOtherCases(final List<Entity> staffAssignmentToShifts) {
        if (staffAssignmentToShifts == null) {
            return EMPTY;
        }

        StringBuilder listOfWorkersWithOtherCases = new StringBuilder();

        for (Entity staffAssignmentToShift : staffAssignmentToShifts) {
            Entity worker = staffAssignmentToShift.getBelongsToField(WORKER);
            listOfWorkersWithOtherCases.append(worker.getStringField("name"));
            listOfWorkersWithOtherCases.append(" ");
            listOfWorkersWithOtherCases.append(worker.getStringField("surname"));
            listOfWorkersWithOtherCases.append(" - ");
            listOfWorkersWithOtherCases.append(staffAssignmentToShift.getStringField(OCCUPATION_TYPE_NAME));

            if (staffAssignmentToShifts.indexOf(staffAssignmentToShift) != (staffAssignmentToShifts.size() - 1)) {
                listOfWorkersWithOtherCases.append(", ");
            }
        }

        return listOfWorkersWithOtherCases.toString();
    }

    public List<String> getListOfWorkerWithOtherCases(final List<Entity> staffAssignmentToShifts) {
        List<String> listOfWorkers = Lists.newArrayList();
        if (staffAssignmentToShifts == null) {
            return listOfWorkers;
        }

        for (Entity staffAssignmentToShift : staffAssignmentToShifts) {
            StringBuilder listOfWorkersWithOtherCases = new StringBuilder();

            Entity worker = staffAssignmentToShift.getBelongsToField(WORKER);
            listOfWorkersWithOtherCases.append(worker.getStringField("name"));
            listOfWorkersWithOtherCases.append(" ");
            listOfWorkersWithOtherCases.append(worker.getStringField("surname"));
            listOfWorkersWithOtherCases.append(" - ");
            listOfWorkersWithOtherCases.append(staffAssignmentToShift.getStringField(OCCUPATION_TYPE_NAME));
            listOfWorkers.add(listOfWorkersWithOtherCases.toString());

        }

        return listOfWorkers;
    }

    public List<Entity> getStaffsList(final Entity assignmentToShift, final String occupationType, final Entity productionLine) {
        List<Entity> staffs = new ArrayList<Entity>();
        if (assignmentToShift == null) {
            return staffs;
        }
        SearchCriterion criterion = SearchRestrictions.eq(OCCUPATION_TYPE, occupationType);
        SearchCriteriaBuilder builder = assignmentToShift.getHasManyField(STAFF_ASSIGNMENT_TO_SHIFTS).find().add(criterion)
                .add(SearchRestrictions.belongsTo(PRODUCTION_LINE, productionLine));
        String assignmentState = assignmentToShift.getStringField(STATE);

        if (AssignmentToShiftState.CORRECTED.getStringValue().equals(assignmentState)) {
            staffs = builder.add(SearchRestrictions.eq(STATE, CORRECTED.getStringValue())).list().getEntities();
        } else if (!AssignmentToShiftState.DRAFT.getStringValue().equals(assignmentState)) {
            staffs = builder.add(SearchRestrictions.eq(STATE, ACCEPTED.getStringValue())).list().getEntities();
        }

        return staffs;
    }

    public List<Entity> getProductionLines() {
        return dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE).find()
                .add(SearchRestrictions.eq("active", true)).list().getEntities();
    }

    public List<Entity> getProductionLinesWithStaff(final Entity productionLine) {
        return dataDefinitionService
                .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_STAFF_ASSIGNMENT_TO_SHIFT)
                .find()
                .add(SearchRestrictions.ne(StaffAssignmentToShiftFields.STATE,
                        StaffAssignmentToShiftState.SIMPLE.getStringValue()))
                .add(SearchRestrictions.belongsTo(StaffAssignmentToShiftFields.PRODUCTION_LINE, productionLine)).list()
                .getEntities();
    }

}
