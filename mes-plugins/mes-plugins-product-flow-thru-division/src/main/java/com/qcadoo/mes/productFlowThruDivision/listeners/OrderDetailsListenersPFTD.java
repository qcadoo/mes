/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision.listeners;

import java.util.Map;
import java.util.Objects;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.OrderMaterialAvailability;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderDetailsListenersPFTD {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderMaterialAvailability orderMaterialAvailability;

    @Autowired
    private WarehouseIssueGenerator warehouseIssueGenerator;

    @Autowired
    private ParameterService parameterService;

    public void showWarehouseIssuesForOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity order = form.getEntity();

        if (order.getId() == null) {
            return;
        }

        String orderNumber = order.getStringField("number");

        if (orderNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put("order", applyInOperator(orderNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.warehouseIssue");
        parameters.put("window.showBack", true);

        String url = "/page/productFlowThruDivision/warehouseIssueList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value) {
        return "[" + value + "]";
    }

    public void showMaterialAvailabilityForProductionTracking(final ViewDefinitionState view, final ComponentState state,
                                                              final String[] args) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity productionRecord = productionRecordForm.getEntity();

        Long orderId = productionRecord.getBelongsToField(ProductionTrackingFields.ORDER).getId();

        showMaterialAvailability(view, orderId);
    }

    public void showMaterialAvailabilityForOrder(final ViewDefinitionState view, final ComponentState state,
                                                 final String[] args) {
        Long orderId = (Long) state.getFieldValue();
        showMaterialAvailability(view, orderId);
    }

    private void showMaterialAvailability(ViewDefinitionState view, Long orderId) {
        orderMaterialAvailability.generateAndSaveMaterialAvailabilityForOrder(getOrderDD().get(orderId));

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("order.id", orderId);

        parameters.put("window.showBack", true);

        String url = "/page/productFlowThruDivision/orderWithMaterialAvailabilityList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    public void onAddStaffExistingEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        resetForm(view);
    }

    public void onRemoveStaffSelectedEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        resetForm(view);
    }

    private void resetForm(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        form.performEvent(view, "reset");
    }
}
