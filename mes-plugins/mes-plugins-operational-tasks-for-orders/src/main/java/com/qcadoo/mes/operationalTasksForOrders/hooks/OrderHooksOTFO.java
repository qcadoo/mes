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

import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.PRODUCTION_LINE;
import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksFieldsOTFOF.ORDER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderHooksOTFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changedProductionLine(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        Entity order = dataDefinition.get(entity.getId());
        Entity productionLine = entity.getBelongsToField(PRODUCTION_LINE);
        Entity orderProductionLine = order.getBelongsToField(PRODUCTION_LINE);
        if (orderProductionLine.equals(productionLine)) {
            return;
        } else {
            changedProductionLineInOperationalTasks(order, productionLine);
        }
    }

    private void changedProductionLineInOperationalTasks(final Entity order, final Entity productionLine) {
        DataDefinition operationalTasksDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        List<Entity> operationalTasksList = operationalTasksDD.find().add(SearchRestrictions.belongsTo(ORDER, order)).list()
                .getEntities();
        for (Entity operationalTask : operationalTasksList) {
            operationalTask.setField(PRODUCTION_LINE, productionLine);
            operationalTasksDD.save(operationalTask);
        }
    }
}
