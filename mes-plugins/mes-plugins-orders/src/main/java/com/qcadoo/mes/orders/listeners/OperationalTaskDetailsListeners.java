/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.orders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.constants.OperationalTaskDtoFields;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.hooks.OperationalTasksDetailsHooks;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationalTaskDetailsListeners {

    

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String PLANNED_QUANTITY_UNIT = "plannedQuantityUNIT";

    private static final String USED_QUANTITY_UNIT = "usedQuantityUNIT";

    @Autowired
    private OperationalTasksDetailsHooks operationalTaskDetailsHooks;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private NumberService numberService;

    public final void onTypeChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        disableFieldsWhenOrderTypeIsSelected(view, state, args);
        disableButtons(view, state, args);
    }

    public final void showOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.ORDER);
        Entity order = orderLookup.getEntity();

        if (Objects.isNull(order)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", order.getId());

        String url = "../page/orders/orderDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOperationParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        if (Objects.isNull(technologyOperationComponent)) {
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
        FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity operationalTask = operationalTaskForm.getEntity();

        if (Objects.isNull(operationalTask.getId())) {
            return;
        }

        Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);

        if (Objects.isNull(order)) {
            return;
        }

        String orderNumber = order.getStringField(OrderFields.NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put(OperationalTaskDtoFields.ORDER_NUMBER, "["+orderNumber+"]");

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "operationalTask.operationalTasks");

        String url = "../page/orders/operationalTasksList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void disableFieldsWhenOrderTypeIsSelected(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        operationalTaskDetailsHooks.disableFieldsWhenOrderTypeIsSelected(view);
    }

    public void onOrderChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.PRODUCT);
        productLookup.setFieldValue(null);
        FieldComponent plannedQuantityField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.PLANNED_QUANTITY);
        plannedQuantityField.setFieldValue(null);
        technologyOperationComponentLookup.setFieldValue(null);
    }

    public void onDivisionChange(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.WORKSTATION);
        FilterValueHolder filterValueHolder = workstationLookup.getFilterValue();
        if(filterValueHolder.has(OperationalTaskFields.DIVISION)) {
            filterValueHolder.remove(OperationalTaskFields.DIVISION);
            workstationLookup.setFilterValue(filterValueHolder);
        }
        workstationLookup.setFieldValue(null);
        workstationLookup.requestComponentUpdateState();
    }

    public void onTechnologyOperationComponentChange(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        operationalTaskDetailsHooks.setNameAndDescription(view);
        setAdditionalFields(view);
    }

    public void setAdditionalFields(final ViewDefinitionState view) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.PRODUCT);
        FieldComponent plannedQuantityField = (FieldComponent) view
                .getComponentByReference(OperationalTaskFields.PLANNED_QUANTITY);
        FieldComponent plannedQuantityUnitField = (FieldComponent) view.getComponentByReference(PLANNED_QUANTITY_UNIT);
        FieldComponent usedQuantityField = (FieldComponent) view.getComponentByReference("usedQuantity");
        FieldComponent usedQuantityUnitField = (FieldComponent) view.getComponentByReference(USED_QUANTITY_UNIT);

        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        if (Objects.isNull(technologyOperationComponent)) {
            productLookup.setFieldValue(null);
            plannedQuantityField.setFieldValue(null);
            plannedQuantityUnitField.setFieldValue(null);
            usedQuantityField.setFieldValue(null);
            usedQuantityUnitField.setFieldValue(null);
        } else {
            Entity mainOutputProductComponent = technologyService.getMainOutputProductComponent(technologyOperationComponent);
            Entity product = mainOutputProductComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);
            productLookup.setFieldValue(product.getId());
        }

        productLookup.requestComponentUpdateState();
        plannedQuantityField.requestComponentUpdateState();
        plannedQuantityUnitField.requestComponentUpdateState();
        usedQuantityField.requestComponentUpdateState();
        usedQuantityUnitField.requestComponentUpdateState();
    }

    private void disableButtons(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        operationalTaskDetailsHooks.disableButtons(view);
    }

}
