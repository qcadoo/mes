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
package com.qcadoo.mes.lineChangeoverNormsForOrders.listeners;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.PREVIOUS_ORDER_FIELDS;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.LINE_CHANGEOVER_NORM;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.ORDER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.PREVIOUS_ORDER;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class LineChangeoverNormsForOrderDetailsListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    @Autowired
    private OrderService orderService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    public final void showPreviousOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FieldComponent previousOrderField = (FieldComponent) view.getComponentByReference(PREVIOUS_ORDER);

        Long previousOrderId = (Long) previousOrderField.getFieldValue();

        if (previousOrderId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", previousOrderId);

        String url = "../page/orders/orderDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showBestFittingLineChangeoverNorm(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent lineChangeoverNormField = (FieldComponent) view.getComponentByReference(LINE_CHANGEOVER_NORM);

        Long lineChangeoverNormId = (Long) lineChangeoverNormField.getFieldValue();

        if (lineChangeoverNormId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", lineChangeoverNormId);

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showLineChangeoverNormForGroup(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent previousOrderTechnologyGroupNumberField = (FieldComponent) view
                .getComponentByReference("previousOrderTechnologyGroupNumber");
        FieldComponent technologyGroupNumberField = (FieldComponent) view.getComponentByReference("technologyGroupNumber");

        String previousOrderTechnologyGroupNumber = (String) previousOrderTechnologyGroupNumberField.getFieldValue();
        String technologyGroupNumber = (String) technologyGroupNumberField.getFieldValue();

        if (StringUtils.isEmpty(previousOrderTechnologyGroupNumber) || StringUtils.isEmpty(technologyGroupNumber)) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put("fromTechnologyGroup", previousOrderTechnologyGroupNumber);
        filters.put("toTechnologyGroup", technologyGroupNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.lineChangeoverNorms");

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showLineChangeoverNormForTechnology(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent previousOrderTechnologyNumberField = (FieldComponent) view
                .getComponentByReference("previousOrderTechnologyNumber");
        FieldComponent technologyNumberField = (FieldComponent) view.getComponentByReference("technologyNumber");

        String previousOrderTechnologyNumber = (String) previousOrderTechnologyNumberField.getFieldValue();
        String technologyNumber = (String) technologyNumberField.getFieldValue();

        if (StringUtils.isEmpty(previousOrderTechnologyNumber) || StringUtils.isEmpty(technologyNumber)) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put("fromTechnology", previousOrderTechnologyNumber);
        filters.put("toTechnology", technologyNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.lineChangeoverNorms");

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void checkIfOrderHasCorrectStateAndIsPrevious(final ViewDefinitionState view,
            final ComponentState componentState, final String[] args) {
        FieldComponent previousOrderField = (FieldComponent) view.getComponentByReference(PREVIOUS_ORDER);
        FieldComponent orderField = (FieldComponent) view.getComponentByReference(ORDER);

        Long previousOrderId = (Long) previousOrderField.getFieldValue();
        Long orderId = (Long) orderField.getFieldValue();

        if ((previousOrderId != null) && (orderId != null)) {
            Entity previousOrderDB = lineChangeoverNormsForOrdersService.getOrderFromDB(previousOrderId);
            Entity orderDB = lineChangeoverNormsForOrdersService.getOrderFromDB(orderId);

            if (!lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrderDB, orderDB)) {
                previousOrderField.addMessage("orders.order.previousOrder.message.orderIsIncorrect",
                        ComponentState.MessageType.FAILURE);
            }
        }
    }

    public final void fillPreviousOrderForm(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        lineChangeoverNormsForOrdersService.fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
    }

    public void showOwnLineChangeoverDurationField(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        orderService.changeFieldState(view, OWN_LINE_CHANGEOVER, OWN_LINE_CHANGEOVER_DURATION);
    }

}
