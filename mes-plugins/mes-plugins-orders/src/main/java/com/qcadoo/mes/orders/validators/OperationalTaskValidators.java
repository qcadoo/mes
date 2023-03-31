/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orders.validators;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OperationalTaskType;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

@Service
public class OperationalTaskValidators {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_COUNT = "count";

    private static final String L_NAME_IS_BLANK_MESSAGE = "orders.operationalTask.error.nameIsBlank";

    private static final String L_WRONG_DATES_ORDER_MESSAGE = "orders.operationalTask.error.finishDateIsEarlier";

    @Autowired
    private OperationalTasksService operationalTasksService;

    public boolean onValidate(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        boolean isValid = hasName(operationalTaskDD, operationalTask);
        isValid = isValid && datesAreInCorrectOrder(operationalTaskDD, operationalTask);
        isValid = isValid && checkIfOrderHasTechnology(operationalTaskDD, operationalTask);
        isValid = isValid && checkIfFieldSet(operationalTaskDD, operationalTask);
        isValid = isValid && checkIfAlreadyExists(operationalTaskDD, operationalTask);
        isValid = isValid && checkWorkstationIsCorrect(operationalTaskDD, operationalTask);
        isValid = isValid && checkStaff(operationalTaskDD, operationalTask);

        return isValid;
    }

    private boolean hasName(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);

        if (OperationalTaskType.OTHER_CASE.getStringValue().equalsIgnoreCase(type) && hasBlankName(operationalTask)) {
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.NAME), L_NAME_IS_BLANK_MESSAGE);

            return false;
        }

        return true;
    }

    private boolean hasBlankName(final Entity operationalTask) {
        return StringUtils.isBlank(operationalTask.getStringField(OperationalTaskFields.NAME));
    }

    public boolean datesAreInCorrectOrder(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
        Date finishDate = operationalTask.getDateField(OperationalTaskFields.FINISH_DATE);

        if (finishDate.before(startDate)) {
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.START_DATE), L_WRONG_DATES_ORDER_MESSAGE);
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.FINISH_DATE), L_WRONG_DATES_ORDER_MESSAGE);

            return false;
        }

        return true;
    }

    public boolean datesAreCorrect(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Entity technologyOperationComponent = operationalTask
                .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

        if (Objects.nonNull(technologyOperationComponent)
                && Objects.nonNull(operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION))) {
            Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
            Date finishDate = operationalTask.getDateField(OperationalTaskFields.FINISH_DATE);
            Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);

            if (Objects.nonNull(technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT))) {
                Entity parent = operationalTasksService.getParent(technologyOperationComponent, order);

                if (Objects.nonNull(parent) && Objects.nonNull(parent.getBelongsToField(OperationalTaskFields.WORKSTATION))) {
                    if (parent.getDateField(OperationalTaskFields.START_DATE).before(startDate)) {
                        operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.START_DATE),
                                "orders.operationalTask.error.inappropriateStartDateNext");

                        return false;
                    }

                    if (parent.getDateField(OperationalTaskFields.FINISH_DATE).before(finishDate)) {
                        operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.FINISH_DATE),
                                "orders.operationalTask.error.inappropriateFinishDateNext");

                        return false;
                    }
                }
            }

            List<Entity> children = operationalTasksService.getChildren(technologyOperationComponent, order);

            for (Entity child : children) {
                if (Objects.nonNull(child.getBelongsToField(OperationalTaskFields.WORKSTATION))) {
                    if (child.getDateField(OperationalTaskFields.START_DATE).after(startDate)) {
                        operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.START_DATE),
                                "orders.operationalTask.error.inappropriateStartDatePrevious");

                        return false;
                    }

                    if (child.getDateField(OperationalTaskFields.FINISH_DATE).after(finishDate)) {
                        operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.FINISH_DATE),
                                "orders.operationalTask.error.inappropriateFinishDatePrevious");

                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkIfOrderHasTechnology(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);

        if (Objects.isNull(order)) {
            return true;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(technology)) {
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.ORDER),
                    "orders.operationalTask.order.error.technologyIsNull");

            return false;
        }

        return true;
    }

    private boolean checkIfFieldSet(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);

        if (operationalTasksService.isOperationalTaskTypeExecutionOperationInOrder(type)) {
            boolean isValid = true;

            Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);

            if (Objects.isNull(order)) {
                operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.ORDER),
                        "qcadooView.validate.field.error.missing");

                isValid = false;
            }

            Entity technologyOperationComponent = operationalTask.getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

            if (Objects.isNull(technologyOperationComponent)) {
                operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT),
                        "qcadooView.validate.field.error.missing");

                isValid = false;
            }

            return isValid;
        }

        return true;
    }

    private boolean checkIfAlreadyExists(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);

        if (OperationalTaskType.EXECUTION_OPERATION_IN_ORDER.getStringValue().equalsIgnoreCase(type)) {
            Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
            Entity technologyOperationComponent = operationalTask.getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

            SearchCriteriaBuilder searchCriteriaBuilder = operationalTaskDD.find()
                    .createAlias(OperationalTaskFields.ORDER, OperationalTaskFields.ORDER, JoinType.LEFT)
                    .createAlias(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, JoinType.LEFT)
                    .add(SearchRestrictions.eq(OperationalTaskFields.ORDER + L_DOT + L_ID, order.getId()))
                    .add(SearchRestrictions.eq(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT + L_DOT + L_ID, technologyOperationComponent.getId()))
                    .add(SearchRestrictions.in(OperationalTaskFields.STATE,
                            Lists.newArrayList(OperationalTaskStateStringValues.PENDING, OperationalTaskStateStringValues.STARTED,
                                    OperationalTaskStateStringValues.FINISHED)));

            if (Objects.nonNull(operationalTask.getId())) {
                searchCriteriaBuilder.add(SearchRestrictions.idNe(operationalTask.getId()));
            }

            searchCriteriaBuilder.setProjection(alias(rowCount(), L_COUNT));
            searchCriteriaBuilder.addOrder(asc(L_COUNT));

            Entity countProjection = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

            boolean isValid = (Objects.isNull(countProjection) || ((Long) countProjection.getField(L_COUNT) == 0));

            if (!isValid) {
                operationalTask.addGlobalError("orders.operationalTask.error.notUnique");
            }

            return isValid;
        }

        return true;
    }

    private boolean checkWorkstationIsCorrect(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Entity technologyOperationComponent = operationalTask
                .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);

        if (Objects.nonNull(technologyOperationComponent) && Objects.nonNull(workstation)) {
            List<Entity> workstations = technologyOperationComponent
                    .getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS);

            if (!workstations.isEmpty() && checkWorkstation(workstations, workstation.getId())) {
                String workstationNumbers = getWorkstationsNumbers(workstations);

                operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.WORKSTATION),
                        "orders.error.inappropriateWorkstationForOperation", workstationNumbers);

                return false;
            }
        }

        return true;
    }

    private boolean checkWorkstation(final List<Entity> workstations, final Long workstationId) {
        return workstations.stream().noneMatch(workstation -> workstation.getId().equals(workstationId));
    }

    private String getWorkstationsNumbers(final List<Entity> workstations) {
        return workstations.stream().map(workstation -> workstation.getStringField(WorkstationFields.NUMBER))
                .collect(Collectors.joining(", "));
    }

    private boolean checkStaff(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Entity technologyOperationComponent = operationalTask
                .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        Integer actualStaff = operationalTask.getIntegerField(OperationalTaskFields.ACTUAL_STAFF);

        int minStaff;

        if (!Objects.isNull(technologyOperationComponent)) {
            minStaff = technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF);
        } else {
            minStaff = 1;
        }

        if (Objects.nonNull(actualStaff) && minStaff > actualStaff) {
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.ACTUAL_STAFF),
                    "orders.operationalTask.error.minStaffBiggerThanActualStaff");

            return false;
        }

        if (Objects.nonNull(actualStaff) && actualStaff % minStaff != 0) {
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.ACTUAL_STAFF),
                    "orders.operationalTask.error.actualStaffMustBeMultipleMinStaff", String.valueOf(minStaff));

            return false;
        }

        return true;
    }

}
