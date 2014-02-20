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
package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyInstanceOperCompFieldsTS;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationalTaskHooksOTFOOverrideUtil {

    @Autowired
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    public void onSaveForSubcontracted(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        String typeTask = operationalTask.getStringField(OperationalTaskFields.TYPE_TASK);

        if (operationalTasksForOrdersService.isOperationalTaskTypeTaskExecutionOperationInOrder(typeTask)) {
            Entity order = operationalTask.getBelongsToField(OperationalTaskFieldsOTFO.ORDER);

            Entity technologyOperationComponent = operationalTask
                    .getBelongsToField(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);

            if ((order == null) || (technologyOperationComponent == null) || isSubcontracting(technologyOperationComponent)) {
                operationalTask.setField(OperationalTaskFields.NAME, null);
                operationalTask.setField(OperationalTaskFields.DESCRIPTION, null);
                operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE, null);
            }
        }
    }

    private boolean isSubcontracting(final Entity technologyOperationComponent) {
        return ((technologyOperationComponent != null) && technologyOperationComponent
                .getBooleanField(TechnologyInstanceOperCompFieldsTS.IS_SUBCONTRACTING));
    }

}
