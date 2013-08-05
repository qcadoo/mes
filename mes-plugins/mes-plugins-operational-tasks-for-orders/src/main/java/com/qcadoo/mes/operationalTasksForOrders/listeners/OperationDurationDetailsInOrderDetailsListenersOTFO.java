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

import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.DESCRIPTION;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.NAME;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.NUMBER;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.PRODUCTION_LINE;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.TYPE_TASK;
import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFOFields.ORDER;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.COMMENT;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.OPERATION;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksForOrdersConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
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
        List<Entity> techOperComps = order.getBelongsToField(OrderFields.TECHNOLOGY).getHasManyField(
                TechnologyFields.OPERATION_COMPONENTS);
        for (Entity techOperComp : techOperComps) {
            deleteTOCOperationalTasks(techOperComp);
            createOperationalTasks(order, techOperComp, techOperComp.getBooleanField("isSubcontracting"));
        }
    }

    private void deleteTOCOperationalTasks(final Entity techOperComp) {
        DataDefinition techOperCompOperationalTasksDD = dataDefinitionService.get(
                OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASKS);
        Entity entity = techOperCompOperationalTasksDD
                .find()
                .add(SearchRestrictions
                        .belongsTo(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT, techOperComp))
                .uniqueResult();
        if (entity != null) {
            techOperCompOperationalTasksDD.delete(entity.getId());
        }
    }

    private void createOperationalTasks(final Entity order, final Entity techOperComp, final boolean isSubcontracting) {
        DataDefinition operationTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        DataDefinition techOperCompOperationalTasksDD = dataDefinitionService.get(
                OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASKS);
        Entity techOperCompTimeCalculations = techOperComp.getBelongsToField("techOperCompTimeCalculations");
        Entity techOperCompOperationalTasks = techOperCompOperationalTasksDD.create();
        techOperCompOperationalTasks.setField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT, techOperComp);
        Entity operationalTask = operationTaskDD.create();
        operationalTask.setField(NUMBER, numberGeneratorService.generateNumber(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK));
        operationalTask.setField(NAME, techOperComp.getBelongsToField(OPERATION).getStringField(NAME));
        operationalTask.setField(OperationalTasksFields.START_DATE, techOperCompTimeCalculations.getField("effectiveDateFrom"));
        operationalTask.setField(OperationalTasksFields.FINISH_DATE, techOperCompTimeCalculations.getField("effectiveDateTo"));
        operationalTask.setField(TYPE_TASK, "02executionOperationInOrder");
        operationalTask.setField(ORDER, order);
        ;
        if (!isSubcontracting) {
            operationalTask.setField(PRODUCTION_LINE, order.getBelongsToField(PRODUCTION_LINE));
        }
        operationalTask.setField(DESCRIPTION, techOperComp.getStringField(COMMENT));
        operationalTask = operationTaskDD.save(operationalTask);

        techOperCompOperationalTasks.setField("operationalTask", Lists.newArrayList(operationalTask));
        techOperCompOperationalTasks.getDataDefinition().save(techOperCompOperationalTasks);
    }
}
