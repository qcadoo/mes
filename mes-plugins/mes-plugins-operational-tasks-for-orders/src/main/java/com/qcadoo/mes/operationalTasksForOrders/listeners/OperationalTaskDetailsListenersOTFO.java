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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.hooks.OperationalTaskDetailsHooksOTFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTaskDetailsListenersOTFO {

    private static final String L_FORM = "form";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private OperationalTaskDetailsHooksOTFO operationalTaskDetailsHooksOTFO;

    public final void showOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);
        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", order.getId());

        String url = "../page/orders/orderDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOperationParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);
        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        if (technologyOperationComponent == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", technologyOperationComponent.getId());

        parameters.put("window.permanentlyDisabled", true);

        String url = "../page/technologies/technologyOperationComponentDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOperationalTasksWithOrder(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity operationalTask = operationalTaskForm.getEntity();

        if (operationalTask.getId() == null) {
            return;
        }

        Entity order = operationalTask.getBelongsToField(OperationalTaskFieldsOTFO.ORDER);

        if (order == null) {
            return;
        }

        String orderNumber = order.getStringField(OrderFields.NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("orderNumber", orderNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "operationalTask.operationalTasks");

        String url = "../page/operationalTasks/operationalTasksList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void disableFieldsWhenOrderTypeIsSelected(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        operationalTaskDetailsHooksOTFO.disableFieldsWhenOrderTypeIsSelected(view);
    }

    public void setOperationalTaskProductionLineAndClearOperation(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent productionLineLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.PRODUCTION_LINE);

        Entity order = orderLookup.getEntity();

        technologyOperationComponentLookup.setFieldValue(null);
        technologyOperationComponentLookup.requestComponentUpdateState();

        if (order == null) {
            productionLineLookup.setFieldValue(null);
        } else {
            Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

            productionLineLookup.setFieldValue(productionLine.getId());
        }

        productionLineLookup.requestComponentUpdateState();
    }

    public void setOperationalTaskNameAndDescription(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.NAME);
        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.DESCRIPTION);

        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        if (technologyOperationComponent == null) {
            nameField.setFieldValue(null);
            descriptionField.setFieldValue(null);
        } else {
            Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

            nameField.setFieldValue(operation.getStringField(OperationFields.NAME));
            descriptionField.setFieldValue(technologyOperationComponent
                    .getStringField(TechnologyOperationComponentFields.COMMENT));
        }

        nameField.requestComponentUpdateState();
        descriptionField.requestComponentUpdateState();
    }

    public void disableButtons(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        operationalTaskDetailsHooksOTFO.disableButtons(view);
    }

}
