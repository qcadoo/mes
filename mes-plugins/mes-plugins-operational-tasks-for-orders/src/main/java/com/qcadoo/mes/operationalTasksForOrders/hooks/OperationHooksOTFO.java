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

import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.NAME;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.OPERATION;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksForOrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OperationHooksOTFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changedNameOperationTasksWhenEntityNameChanged(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        Entity operation = dataDefinition.get(entity.getId());
        String entityName = entity.getStringField(NAME);
        String operationName = operation.getStringField(NAME);

        if (!entityName.equals(operationName)) {
            changedNameOperationTasks(entity);
        }
    }

    private void changedNameOperationTasks(final Entity operation) {
        DataDefinition techOperCompDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

        DataDefinition operationalTasksDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);

        DataDefinition techOperCompOperationalTaskDD = dataDefinitionService.get(
                OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASKS);

        List<Entity> techOperCompsWithOperation = techOperCompDD.find().add(SearchRestrictions.belongsTo(OPERATION, operation))
                .list().getEntities();

        for (Entity techOperComp : techOperCompsWithOperation) {

            List<Entity> techOperCompOperationalTasks = techOperCompOperationalTaskDD.find()
                    .add(SearchRestrictions.belongsTo("technologyOperationComponent", techOperComp)).list().getEntities();

            for (Entity techOperCompOperationalTask : techOperCompOperationalTasks) {

                List<Entity> operationalTasksList = operationalTasksDD.find()
                        .add(SearchRestrictions.belongsTo("techOperCompOperationalTasks", techOperCompOperationalTask)).list()
                        .getEntities();

                for (Entity operationalTask : operationalTasksList) {
                    operationalTask.setField(NAME, operation.getStringField(NAME));
                    operationalTasksDD.save(operationalTask);
                }
            }
        }
    }
}
