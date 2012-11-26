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

import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFOFields.ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderDetailsListenersOTFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void deleteOperationTasksWhenTechnologyIsChanged(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        Entity order = form.getEntity().getDataDefinition().get(form.getEntityId());
        Entity technology = order.getBelongsToField(TECHNOLOGY);
        if (technology == null) {
            return;
        }
        Entity technologyFromLookup = ((LookupComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY)).getEntity();
        if (technologyFromLookup == null || !technologyFromLookup.equals(technology)) {
            deleteOperationTaskForOrder(order);
        }
    }

    private void deleteOperationTaskForOrder(final Entity order) {
        DataDefinition operationTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        List<Entity> operationTasksList = operationTaskDD.find().add(SearchRestrictions.belongsTo(ORDER, order)).list()
                .getEntities();
        for (Entity operationalTask : operationTasksList) {
            operationTaskDD.delete(operationalTask.getId());
        }
    }
}
