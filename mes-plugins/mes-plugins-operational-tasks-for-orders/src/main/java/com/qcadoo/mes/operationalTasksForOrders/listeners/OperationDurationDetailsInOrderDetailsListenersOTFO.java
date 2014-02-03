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
package com.qcadoo.mes.operationalTasksForOrders.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskTypeTaskOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksForOrdersConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompTimeCalculationsFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OperationDurationDetailsInOrderDetailsListenersOTFO {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

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
                deleteTechOperCompOperationalTasks(technologyOperationComponent);

                createOperationalTasks(order, technologyOperationComponent,
                        technologyOperationComponent.getBooleanField("isSubcontracting"));
            }
        }
    }

    private void deleteTechOperCompOperationalTasks(final Entity technologyOperationComponent) {
        DataDefinition techOperCompOperationalTasksDD = dataDefinitionService.get(
                OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASKS);

        Entity techOperCompOperationalTasks = techOperCompOperationalTasksDD
                .find()
                .add(SearchRestrictions.belongsTo(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent)).setMaxResults(1).uniqueResult();

        if (techOperCompOperationalTasks != null) {
            techOperCompOperationalTasksDD.delete(techOperCompOperationalTasks.getId());
        }
    }

    private void createOperationalTasks(final Entity order, final Entity technologyOperationComponent,
            final boolean isSubcontracting) {
        Entity techOperCompTimeCalculations = technologyOperationComponent
                .getBelongsToField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATIONS);

        DataDefinition operationTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);

        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        Entity operationalTask = operationTaskDD.create();

        operationalTask.setField(OperationalTaskFields.NUMBER, numberGeneratorService.generateNumber(
                OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK));
        operationalTask.setField(OperationalTaskFields.NAME, operation.getStringField(OperationFields.NAME));
        operationalTask.setField(OperationalTaskFields.START_DATE,
                techOperCompTimeCalculations.getField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM));
        operationalTask.setField(OperationalTaskFields.FINISH_DATE,
                techOperCompTimeCalculations.getField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_TO));
        operationalTask.setField(OperationalTaskFields.TYPE_TASK,
                OperationalTaskTypeTaskOTFO.EXECUTION_OPERATION_IN_ORDER.getStringValue());
        operationalTask.setField(OperationalTaskFieldsOTFO.ORDER, order);

        if (!isSubcontracting) {
            operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE, order.getBelongsToField(OrderFields.PRODUCTION_LINE));
        }

        operationalTask.setField(OperationalTaskFields.DESCRIPTION,
                technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT));

        operationalTask = operationalTask.getDataDefinition().save(operationalTask);

        createTechOperCompOperationalTasks(technologyOperationComponent, operationalTask);
    }

    private void createTechOperCompOperationalTasks(final Entity technologyOperationComponent, final Entity operationalTask) {
        DataDefinition techOperCompOperationalTasksDD = dataDefinitionService.get(
                OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASKS);

        Entity techOperCompOperationalTasks = techOperCompOperationalTasksDD.create();

        techOperCompOperationalTasks.setField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT,
                technologyOperationComponent);
        techOperCompOperationalTasks.setField(TechOperCompOperationalTasksFields.OPERATIONAL_TASKS,
                Lists.newArrayList(operationalTask));

        techOperCompOperationalTasks.getDataDefinition().save(techOperCompOperationalTasks);
    }

}
