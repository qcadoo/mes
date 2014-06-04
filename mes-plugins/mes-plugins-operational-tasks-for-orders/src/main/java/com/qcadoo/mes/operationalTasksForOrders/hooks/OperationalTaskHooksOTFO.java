/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationalTaskHooksOTFO {

    @Autowired
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    public void onSave(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        fillTechOperCompOperationalTasks(operationalTask);
        fillNameAndDescription(operationalTask);
        fillProductionLine(operationalTask);
    }

    public void fillTechOperCompOperationalTasks(final Entity operationalTask) {
        String typeTask = operationalTask.getStringField(OperationalTaskFields.TYPE_TASK);

        if (operationalTasksForOrdersService.isOperationalTaskTypeTaskExecutionOperationInOrder(typeTask)) {
            Entity technologyOperationComponent = operationalTask
                    .getBelongsToField(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);

            Entity techOperCompOperationalTask = operationalTask
                    .getBelongsToField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK);

            if (technologyOperationComponent == null) {
                if (techOperCompOperationalTask != null) {
                    techOperCompOperationalTask.getDataDefinition().delete(techOperCompOperationalTask.getId());
                }

                operationalTask.setField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK, null);
            } else {
                if (techOperCompOperationalTask == null) {
                    techOperCompOperationalTask = operationalTasksForOrdersService.getTechOperCompOperationalTaskDD().create();
                }

                techOperCompOperationalTask.setField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent);

                techOperCompOperationalTask = techOperCompOperationalTask.getDataDefinition().save(techOperCompOperationalTask);

                operationalTask.setField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK, techOperCompOperationalTask);
            }
        }
    }

    private void fillNameAndDescription(final Entity operationalTask) {
        String typeTask = operationalTask.getStringField(OperationalTaskFields.TYPE_TASK);

        if (operationalTasksForOrdersService.isOperationalTaskTypeTaskExecutionOperationInOrder(typeTask)) {
            Entity techOperCompOperationalTask = operationalTask
                    .getBelongsToField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK);

            if (techOperCompOperationalTask == null) {
                operationalTask.setField(OperationalTaskFields.NAME, null);
                operationalTask.setField(OperationalTaskFields.DESCRIPTION, null);
            } else {
                Entity technologyOperationComponent = techOperCompOperationalTask
                        .getBelongsToField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT);

                Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                operationalTask.setField(OperationalTaskFields.NAME, operation.getStringField(OperationFields.NAME));
                operationalTask.setField(OperationalTaskFields.DESCRIPTION,
                        technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT));
            }
        }
    }

    private void fillProductionLine(final Entity operationalTask) {
        String typeTask = operationalTask.getStringField(OperationalTaskFields.TYPE_TASK);

        if (operationalTasksForOrdersService.isOperationalTaskTypeTaskExecutionOperationInOrder(typeTask)) {
            Entity order = operationalTask.getBelongsToField(OperationalTaskFieldsOTFO.ORDER);

            if (order == null) {
                operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE, null);
            } else {
                Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

                operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE, productionLine);
            }
        }
    }

    public void onCopy(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        String typeTask = operationalTask.getStringField(OperationalTaskFields.TYPE_TASK);

        if (operationalTasksForOrdersService.isOperationalTaskTypeTaskExecutionOperationInOrder(typeTask)) {
            Entity techOperCompOperationalTask = operationalTask
                    .getBelongsToField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK);

            if (techOperCompOperationalTask != null) {
                Entity technologyOperationComponent = techOperCompOperationalTask
                        .getBelongsToField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (technologyOperationComponent != null) {
                    operationalTask.setField(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT,
                            technologyOperationComponent);
                    operationalTask.setField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK,
                            operationalTasksForOrdersService.createTechOperCompOperationalTask(technologyOperationComponent));
                }
            }
        }
    }

    public boolean onDelete(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        boolean isDeleted = true;

        Entity techOperCompOperationalTask = operationalTask
                .getBelongsToField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK);

        if (techOperCompOperationalTask != null) {
            isDeleted = techOperCompOperationalTask.getDataDefinition().delete(techOperCompOperationalTask.getId())
                    .isSuccessfull();
        }

        return isDeleted;
    }

}
