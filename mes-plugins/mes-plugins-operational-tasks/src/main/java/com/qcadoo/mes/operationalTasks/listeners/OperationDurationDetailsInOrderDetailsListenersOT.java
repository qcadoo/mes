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
package com.qcadoo.mes.operationalTasks.listeners;

import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskType;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.timeNormsForOperations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationDurationDetailsInOrderDetailsListenersOT {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    public void createOperationalTasks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
            return;
        }

        Entity order = orderForm.getEntity().getDataDefinition().get(orderId);

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology != null) {
            List<Entity> technologyOperationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

            for (Entity technologyOperationComponent : technologyOperationComponents) {
                createOperationalTasks(order, technologyOperationComponent,
                        technologyOperationComponent.getBooleanField("isSubcontracting"));
            }

            orderForm.addMessage("productionScheduling.operationDurationDetailsInOrder.info.operationalTasksCreated",
                    MessageType.SUCCESS);
        }
    }

    private void createOperationalTasks(final Entity order, final Entity technologyOperationComponent,
            final boolean isSubcontracting) {
        Entity techOperCompTimeCalculation = operationWorkTimeService.createOrGetOperCompTimeCalculation(order,
                technologyOperationComponent);

        DataDefinition operationTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);

        Entity operationalTask = operationTaskDD.create();

        operationalTask.setField(OperationalTaskFields.NUMBER, numberGeneratorService
                .generateNumber(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK));

        if (techOperCompTimeCalculation != null) {
            operationalTask.setField(OperationalTaskFields.START_DATE,
                    techOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM));
            operationalTask.setField(OperationalTaskFields.FINISH_DATE,
                    techOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_TO));
        }

        operationalTask.setField(OperationalTaskFields.TYPE,
                OperationalTaskType.EXECUTION_OPERATION_IN_ORDER.getStringValue());
        operationalTask.setField(OperationalTaskFields.ORDER, order);

        if (!isSubcontracting) {
            operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE, order.getBelongsToField(OrderFields.PRODUCTION_LINE));
        }

        operationalTask.setField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);

        operationalTask.getDataDefinition().save(operationalTask);
    }

}
