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
package com.qcadoo.mes.assignmentToShift.print.xls;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AssignmentToShiftXlsHelper {

    private static final String L_EMPTY = "";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> getAssignmentToShift(final Entity shift, final Entity factory, final DateTime date) {
        boolean shiftWorks = new Shift(shift).worksAt(date.dayOfWeek().get());

        if (shiftWorks) {
            List<Entity> assignmentsToShift = dataDefinitionService
                    .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT)
                    .find().add(SearchRestrictions.belongsTo(AssignmentToShiftFields.SHIFT, shift))
                    .add(SearchRestrictions.belongsTo(AssignmentToShiftFields.FACTORY, factory))
                    .add(SearchRestrictions.or(
                            SearchRestrictions.eq(AssignmentToShiftFields.STATE,
                                    AssignmentToShiftState.ACCEPTED.getStringValue()),
                            SearchRestrictions.eq(AssignmentToShiftFields.STATE,
                                    AssignmentToShiftState.CORRECTED.getStringValue())))
                    .add(SearchRestrictions.le(AssignmentToShiftFields.START_DATE, date.toDate()))
                    .addOrder(SearchOrders.desc(AssignmentToShiftFields.START_DATE)).list().getEntities();
            return findCurrentAssignmentsToShift(date.toDate(), assignmentsToShift);

        } else {
            return Lists.newArrayList();
        }
    }

    private List<Entity> findCurrentAssignmentsToShift(final Date date, List<Entity> assignmentsToShift) {
        List<Entity> currentAssignments = Lists.newArrayList();

        Map<Long, List<Entity>> assignmentsForCrews = assignmentsToShift.stream()
                .filter(assignment -> assignment.getBelongsToField(AssignmentToShiftFields.CREW) != null)
                .collect(Collectors.groupingBy(assignment -> assignment.getBelongsToField(AssignmentToShiftFields.CREW).getId()));
        assignmentsForCrews.put(null,
                assignmentsToShift.stream()
                        .filter(assignment -> assignment.getBelongsToField(AssignmentToShiftFields.CREW) == null)
                        .collect(Collectors.toList()));
        List<Entity> crews = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CREW).find().list()
                .getEntities();
        for (Entity crew : crews) {
            currentAssignments.addAll(findCurrentAssignmentsToShiftForCrew(date, assignmentsForCrews.get(crew.getId())));
        }
        currentAssignments.addAll(findCurrentAssignmentsToShiftForCrew(date, assignmentsForCrews.get(null)));
        return currentAssignments;
    }

    private List<Entity> findCurrentAssignmentsToShiftForCrew(final Date date, List<Entity> assignmentsToShiftForCrew) {
        List<Entity> currentAssignments = Lists.newArrayList();
        if (assignmentsToShiftForCrew == null) {
            return currentAssignments;
        }
        Date currentDate = date;
        for (Entity assignmentToShift : assignmentsToShiftForCrew) {
            Date assignmentDate = assignmentToShift.getDateField(AssignmentToShiftFields.START_DATE);
            if (assignmentDate.before(currentDate)) {
                if (currentAssignments.isEmpty()) {
                    currentAssignments.add(assignmentToShift);
                } else {
                    return currentAssignments;
                }
            } else if (assignmentDate.compareTo(currentDate) == 0) {
                currentAssignments.add(assignmentToShift);
            }
            currentDate = assignmentDate;
        }
        return currentAssignments;
    }

    public String getListOfWorkers(final List<Entity> staffAssignmentToShifts) {
        if (staffAssignmentToShifts == null) {
            return L_EMPTY;
        }

        StringBuilder listOfWorkers = new StringBuilder();

        for (Entity staffAssignmentToShift : staffAssignmentToShifts) {
            Entity worker = staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.WORKER);

            listOfWorkers.append(worker.getStringField(StaffFields.NAME));
            listOfWorkers.append(" ");
            listOfWorkers.append(worker.getStringField(StaffFields.SURNAME));

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
            Entity workerEntity = staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.WORKER);

            StringBuilder worker = new StringBuilder();

            worker.append(workerEntity.getStringField(StaffFields.NAME));
            worker.append(" ");
            worker.append(workerEntity.getStringField(StaffFields.SURNAME));

            String description = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.DESCRIPTION);

            if (StringUtils.isNotEmpty(description)) {
                worker.append(", ");
                worker.append(description);
            }

            listOfWorkers.add(worker.toString());
        }

        return listOfWorkers;
    }

    public List<String> getListOfWorkerWithOtherCases(final List<Entity> staffAssignmentToShifts) {
        List<String> listOfWorkers = Lists.newArrayList();

        if (staffAssignmentToShifts == null) {
            return listOfWorkers;
        }

        for (Entity staffAssignmentToShift : staffAssignmentToShifts) {
            StringBuilder listOfWorkersWithOtherCases = new StringBuilder();

            Entity worker = staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.WORKER);

            String occupationTypeName = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME);
            String description = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.DESCRIPTION);

            listOfWorkersWithOtherCases.append(worker.getStringField(StaffFields.NAME));
            listOfWorkersWithOtherCases.append(" ");
            listOfWorkersWithOtherCases.append(worker.getStringField(StaffFields.SURNAME));

            if (StringUtils.isNotEmpty(description)) {
                listOfWorkersWithOtherCases.append(", ");
                listOfWorkersWithOtherCases.append(description);
            }

            if (StringUtils.isNotEmpty(occupationTypeName)) {
                listOfWorkersWithOtherCases.append(" - ");
                listOfWorkersWithOtherCases.append(occupationTypeName);
            }

            listOfWorkers.add(listOfWorkersWithOtherCases.toString());

        }

        return listOfWorkers;
    }

    public List<Entity> getStaffsList(final Entity assignmentToShift, final String occupationType, final Entity productionLine) {
        List<Entity> staffs = Lists.newArrayList();

        if (assignmentToShift == null) {
            return staffs;
        }

        SearchCriterion criterion = SearchRestrictions.eq(StaffAssignmentToShiftFields.OCCUPATION_TYPE, occupationType);

        SearchCriteriaBuilder builder = assignmentToShift.getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS)
                .find().add(criterion)
                .add(SearchRestrictions.belongsTo(StaffAssignmentToShiftFields.PRODUCTION_LINE, productionLine));

        String assignmentState = assignmentToShift.getStringField(AssignmentToShiftFields.STATE);

        if (AssignmentToShiftState.CORRECTED.getStringValue().equals(assignmentState)) {
            staffs = builder.add(SearchRestrictions.eq(StaffAssignmentToShiftFields.STATE,
                    StaffAssignmentToShiftState.CORRECTED.getStringValue())).list().getEntities();
        } else if (!AssignmentToShiftState.DRAFT.getStringValue().equals(assignmentState)) {
            staffs = builder.add(SearchRestrictions.eq(StaffAssignmentToShiftFields.STATE,
                    StaffAssignmentToShiftState.ACCEPTED.getStringValue())).list().getEntities();
        }

        return staffs;
    }

    public List<Entity> getProductionLines() {
        return dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE).find()
                .add(SearchRestrictions.eq("active", true)).list().getEntities();
    }

}
