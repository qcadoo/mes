/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void createOperationalTasks(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity().getDataDefinition().get(form.getEntityId());
        List<Entity> techInstOperComps = order.getHasManyField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
        for (Entity techInstOperComp : techInstOperComps) {
            deleteOperationalTasks(techInstOperComp);
            createOperationalTasks(order, techInstOperComp, techInstOperComp.getBooleanField("isSubcontracting"));
        }
    }

    private void deleteOperationalTasks(final Entity techInstOperComp) {
        DataDefinition operationTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        List<Entity> operationTasks = operationTaskDD
                .find()
                .add(SearchRestrictions.belongsTo(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                        techInstOperComp)).list().getEntities();
        for (Entity operationTask : operationTasks) {
            operationTaskDD.delete(operationTask.getId());
        }
    }

    private void createOperationalTasks(final Entity order, final Entity techInstOperComp, final boolean isSubcontracting) {
        DataDefinition operationTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        Entity operationalTask = operationTaskDD.create();
        operationalTask.setField(OperationalTasksFields.NUMBER, numberGeneratorService.generateNumber(
                OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK));
        operationalTask.setField(
                OperationalTasksFields.NAME,
                techInstOperComp.getBelongsToField(TechnologyInstanceOperCompFields.OPERATION).getStringField(
                        OperationFields.NAME));
        operationalTask.setField(OperationalTasksFields.START_DATE, techInstOperComp.getField("effectiveDateFrom"));
        operationalTask.setField(OperationalTasksFields.FINISH_DATE, techInstOperComp.getField("effectiveDateTo"));
        operationalTask.setField(OperationalTasksFields.TYPE_TASK, "02executionOperationInOrder");
        operationalTask.setField(OperationalTasksOTFRFields.ORDER, order);
        operationalTask.setField(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT, techInstOperComp);
        if (!isSubcontracting) {
            operationalTask
                    .setField(OperationalTasksFields.PRODUCTION_LINE, order.getBelongsToField(OrderFields.PRODUCTION_LINE));
        }
        operationalTask.setField(OperationalTasksFields.DESCRIPTION,
                techInstOperComp.getStringField(TechnologyInstanceOperCompFields.COMMENT));
        operationTaskDD.save(operationalTask);
    }
}
