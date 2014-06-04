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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationHooksOTFO {

    @Autowired
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    public void changedNameInOperationalTasksWhenChanged(final DataDefinition operationDD, final Entity operation) {
        Long operationId = operation.getId();

        if (operationId == null) {
            return;
        }

        Entity operationFromDB = operationDD.get(operationId);

        String name = operation.getStringField(OperationFields.NAME);
        String operationName = operationFromDB.getStringField(OperationFields.NAME);

        if (!name.equals(operationName)) {
            changedNameInOperationalTasks(operation);
        }
    }

    private void changedNameInOperationalTasks(final Entity operation) {
        List<Entity> technologyOperationComponents = operationalTasksForOrdersService
                .getTechnologyOperationComponentsForOperation(operation);

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            List<Entity> techOperCompOperationalTasks = operationalTasksForOrdersService
                    .getTechOperCompOperationalTasksForTechnologyOperationComponent(technologyOperationComponent);

            for (Entity techOperCompOperationalTask : techOperCompOperationalTasks) {
                List<Entity> operationalTasksList = operationalTasksForOrdersService
                        .getOperationalTasksForTechOperCompOperationalTasks(techOperCompOperationalTask);

                for (Entity operationalTask : operationalTasksList) {
                    operationalTask.setField(OperationalTaskFields.NAME, operation.getStringField(OperationFields.NAME));

                    operationalTask.getDataDefinition().save(operationalTask);
                }
            }
        }
    }

}
