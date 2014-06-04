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
package com.qcadoo.mes.operationalTasksForOrders.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.orders.constants.OrderFields;
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

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void deleteOperationTasksWhenTechnologyIsChanged(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
            return;
        }

        Entity orderFromDB = orderForm.getEntity().getDataDefinition().get(orderId);

        Entity technology = technologyLookup.getEntity();
        Entity orderTechnology = orderFromDB.getBelongsToField(OrderFields.TECHNOLOGY);

        if (orderTechnology == null) {
            return;
        }

        if ((technology == null) || !orderTechnology.getId().equals(technology.getId())) {
            deleteOperationTaskForOrder(orderFromDB);
        }
    }

    private void deleteOperationTaskForOrder(final Entity order) {
        DataDefinition operationTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);

        List<Entity> operationTasks = operationTaskDD.find()
                .add(SearchRestrictions.belongsTo(OperationalTaskFieldsOTFO.ORDER, order)).list().getEntities();

        for (Entity operationalTask : operationTasks) {
            operationTaskDD.delete(operationalTask.getId());
        }
    }

}
