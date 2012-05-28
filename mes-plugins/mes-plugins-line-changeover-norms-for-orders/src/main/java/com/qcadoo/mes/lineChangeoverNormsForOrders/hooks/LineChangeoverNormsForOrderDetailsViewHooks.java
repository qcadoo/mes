/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.DURATION;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.ORDER_FIELDS;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.PREVIOUS_ORDER_FIELDS;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.LINE_CHANGEOVER_NORM;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.ORDER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.PREVIOUS_ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class LineChangeoverNormsForOrderDetailsViewHooks {

    private static final String L_FORM = "form";

    @Autowired
    private OrderService orderService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Autowired
    private ChangeoverNormsService lineChangeoverNormsService;

    public final void fillOrderForms(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent orderField = (FieldComponent) view.getComponentByReference(ORDER);
        FieldComponent previousOrderField = (FieldComponent) view.getComponentByReference(PREVIOUS_ORDER);

        if (orderForm.getEntityId() != null) {
            Long orderId = orderForm.getEntityId();

            Entity order = lineChangeoverNormsForOrdersService.getOrderFromDB(orderId);

            if (order != null) {
                orderField.setFieldValue(order.getId());
            }

            lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

            if (previousOrderField.getFieldValue() == null) {
                Entity previousOrder = lineChangeoverNormsForOrdersService.getPreviousOrderFromDB(order);

                if (previousOrder != null) {
                    previousOrderField.setFieldValue(order.getId());
                }

                lineChangeoverNormsForOrdersService.fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
            }
        }
    }

    public void fillLineChangeoverNorm(final ViewDefinitionState view) {
        FieldComponent orderField = (FieldComponent) view.getComponentByReference(ORDER);
        FieldComponent previousOrderField = (FieldComponent) view.getComponentByReference(PREVIOUS_ORDER);

        FieldComponent lineChangeoverNormField = (FieldComponent) view.getComponentByReference("lineChangeoverNorm");
        FieldComponent lineChangeoverNormDurationField = (FieldComponent) view
                .getComponentByReference("lineChangeoverNormDuration");
        if ((orderField.getFieldValue() != null) && (previousOrderField.getFieldValue() != null)) {
            Entity order = lineChangeoverNormsForOrdersService.getOrderFromDB((Long) orderField.getFieldValue());
            Entity previousOrder = lineChangeoverNormsForOrdersService.getOrderFromDB((Long) previousOrderField.getFieldValue());

            if ((order != null) && (previousOrder != null)) {
                Long fromTechnologyId = previousOrder.getBelongsToField(TECHNOLOGY).getId();
                Long toTechnologyId = order.getBelongsToField(TECHNOLOGY).getId();

                Entity fromTechnology = lineChangeoverNormsForOrdersService.getTechnologyFromDB(fromTechnologyId);
                Entity toTechnology = lineChangeoverNormsForOrdersService.getTechnologyFromDB(toTechnologyId);

                Entity productionLine = order.getBelongsToField(PRODUCTION_LINE);

                Entity lineChangeoverNorm = lineChangeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology,
                        productionLine);

                if (lineChangeoverNorm != null) {
                    lineChangeoverNormField.setFieldValue(lineChangeoverNorm.getId());
                    lineChangeoverNormDurationField.setFieldValue(lineChangeoverNorm.getField(DURATION));
                }
            }
        }
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FieldComponent previousOrderField = (FieldComponent) view.getComponentByReference(PREVIOUS_ORDER);

        FieldComponent lineChangeoverNorm = (FieldComponent) view.getComponentByReference(LINE_CHANGEOVER_NORM);

        FieldComponent previousOrderTechnologyGroupNumberField = (FieldComponent) view
                .getComponentByReference("previousOrderTechnologyGroupNumber");
        FieldComponent technologyGroupNumberField = (FieldComponent) view.getComponentByReference("technologyGroupNumber");

        FieldComponent previousOrderTechnologyNumberField = (FieldComponent) view
                .getComponentByReference("previousOrderTechnologyNumber");
        FieldComponent technologyNumberField = (FieldComponent) view.getComponentByReference("technologyNumber");

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup orders = (RibbonGroup) window.getRibbon().getGroupByName("orders");
        RibbonGroup lineChangeoverNorms = (RibbonGroup) window.getRibbon().getGroupByName("lineChangeoverNorms");

        RibbonActionItem showPreviousOrder = (RibbonActionItem) orders.getItemByName("showPreviousOrder");

        RibbonActionItem showBestFittingChangeoverNorm = (RibbonActionItem) lineChangeoverNorms
                .getItemByName("showBestFittingLineChangeoverNorm");
        RibbonActionItem showChangeoverNormForGroup = (RibbonActionItem) lineChangeoverNorms
                .getItemByName("showLineChangeoverNormForGroup");
        RibbonActionItem showChangeoverNormForTechnology = (RibbonActionItem) lineChangeoverNorms
                .getItemByName("showLineChangeoverNormForTechnology");

        if (previousOrderField.getFieldValue() == null) {
            updateButtonState(showPreviousOrder, false);
        } else {
            updateButtonState(showPreviousOrder, true);
        }

        if (lineChangeoverNorm.getFieldValue() == null) {
            updateButtonState(showBestFittingChangeoverNorm, false);
        } else {
            updateButtonState(showBestFittingChangeoverNorm, true);
        }

        if ((previousOrderTechnologyGroupNumberField.getFieldValue() == null)
                || (technologyGroupNumberField.getFieldValue() == null)) {
            updateButtonState(showChangeoverNormForGroup, false);
        } else {
            updateButtonState(showChangeoverNormForGroup, true);
        }

        if ((previousOrderTechnologyNumberField.getFieldValue() == null) || (technologyNumberField.getFieldValue() == null)) {
            updateButtonState(showChangeoverNormForTechnology, false);
        } else {
            updateButtonState(showChangeoverNormForTechnology, true);
        }

    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void showOwnLineChangeoverDurationField(final ViewDefinitionState view) {
        orderService.changeFieldState(view, OWN_LINE_CHANGEOVER, OWN_LINE_CHANGEOVER_DURATION);
    }
}
