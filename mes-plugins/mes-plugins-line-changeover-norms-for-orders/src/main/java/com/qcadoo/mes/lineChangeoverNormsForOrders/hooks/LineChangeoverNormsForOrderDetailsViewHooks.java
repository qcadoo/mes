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
package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsSearchService;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class LineChangeoverNormsForOrderDetailsViewHooks {

    private static final String L_FORM = "form";

    @Autowired
    private OrderService orderService;

    @Autowired
    private ChangeoverNormsService changeoverNormsService;

    @Autowired
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    public final void fillOrderForms(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OrderFieldsLCNFO.ORDER);
        LookupComponent previousOrderLookup = (LookupComponent) view.getComponentByReference(OrderFieldsLCNFO.PREVIOUS_ORDER);

        if (orderForm.getEntityId() == null) {
            return;
        }
        Entity order = orderForm.getPersistedEntityWithIncludedFormValues();

        orderLookup.setFieldValue(order.getId());
        orderLookup.requestComponentUpdateState();

        lineChangeoverNormsForOrdersService.fillOrderForm(view, LineChangeoverNormsForOrdersConstants.ORDER_FIELDS);

        if (previousOrderLookup.isEmpty()) {
            Entity previousOrder = lineChangeoverNormsForOrdersService.getPreviousOrderFromDB(order);

            if (previousOrder != null) {
                previousOrderLookup.setFieldValue(previousOrder.getId());
                previousOrderLookup.requestComponentUpdateState();

                lineChangeoverNormsForOrdersService.fillOrderForm(view, LineChangeoverNormsForOrdersConstants.PREVIOUS_ORDER_FIELDS);
            }
        }
    }

    public void fillLineChangeoverNorm(final ViewDefinitionState view) {
        Entity lineChangeoverNorm = findChangeoverNorm(view);
        fillChangeoverNormFields(view, lineChangeoverNorm);
    }

    private Entity findChangeoverNorm(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OrderFieldsLCNFO.ORDER);
        LookupComponent previousOrderLookup = (LookupComponent) view.getComponentByReference(OrderFieldsLCNFO.PREVIOUS_ORDER);

        Entity order = orderLookup.getEntity();

        if (order == null) {
            return null;
        }

        Entity fromTechnology = extractTechnologyPrototypeFrom(previousOrderLookup.getEntity());
        Entity toTechnology = extractTechnologyPrototypeFrom(order);

        if (fromTechnology == null || toTechnology == null) {
            return null;
        }

        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        return changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
    }

    private Entity extractTechnologyPrototypeFrom(final Entity order) {
        if (order == null) {
            return null;
        }

        return order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
    }

    private void fillChangeoverNormFields(final ViewDefinitionState view, final Entity lineChangeoverNorm) {
        FieldComponent lineChangeoverNormField = (FieldComponent) view.getComponentByReference("lineChangeoverNorm");
        FieldComponent lineChangeoverNormDurationField = (FieldComponent) view
                .getComponentByReference("lineChangeoverNormDuration");

        if (lineChangeoverNorm == null) {
            lineChangeoverNormField.setFieldValue(null);
            lineChangeoverNormDurationField.setFieldValue(null);
        } else {
            lineChangeoverNormField.setFieldValue(lineChangeoverNorm.getId());
            lineChangeoverNormDurationField.setFieldValue(lineChangeoverNorm.getField(LineChangeoverNormsFields.DURATION));
        }

        lineChangeoverNormField.requestComponentUpdateState();
        lineChangeoverNormDurationField.requestComponentUpdateState();
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FieldComponent productionLineField = (FieldComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);

        LookupComponent previousOrderLookup = (LookupComponent) view.getComponentByReference(OrderFieldsLCNFO.PREVIOUS_ORDER);

        LookupComponent lineChangeoverNormLookup = (LookupComponent) view.getComponentByReference(OrderFieldsLCNFO.LINE_CHANGEOVER_NORM);

        FieldComponent previousOrderTechnologyGroupNumberField = (FieldComponent) view
                .getComponentByReference("previousOrderTechnologyGroupNumber");
        FieldComponent technologyGroupNumberField = (FieldComponent) view.getComponentByReference("technologyGroupNumber");

        FieldComponent previousOrderTechnologyNumberField = (FieldComponent) view
                .getComponentByReference("previousOrderTechnologyNumber");
        FieldComponent technologyNumberField = (FieldComponent) view.getComponentByReference("technologyNumber");

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup orders = window.getRibbon().getGroupByName("orders");
        RibbonGroup lineChangeoverNorms = window.getRibbon().getGroupByName("lineChangeoverNorms");

        RibbonActionItem showPreviousOrder = orders.getItemByName("showPreviousOrder");

        RibbonActionItem showBestFittingLineChangeoverNorm = lineChangeoverNorms
                .getItemByName("showBestFittingLineChangeoverNorm");
        RibbonActionItem showChangeoverNormForGroup = lineChangeoverNorms.getItemByName("showLineChangeoverNormForGroup");
        RibbonActionItem showChangeoverNormForTechnology = lineChangeoverNorms
                .getItemByName("showLineChangeoverNormForTechnology");

        Entity productionLine = lineChangeoverNormsForOrdersService.getProductionLineFromDB((Long) productionLineField
                .getFieldValue());

        boolean hasDefinedPreviousOrder = !previousOrderLookup.isEmpty();
        updateButtonState(showPreviousOrder, hasDefinedPreviousOrder);

        boolean hasMatchingChangeoverNorm = !lineChangeoverNormLookup.isEmpty();
        updateButtonState(showBestFittingLineChangeoverNorm, hasMatchingChangeoverNorm);

        if (StringUtils.isEmpty((String) previousOrderTechnologyGroupNumberField.getFieldValue())
                || StringUtils.isEmpty((String) technologyGroupNumberField.getFieldValue())) {
            updateButtonState(showChangeoverNormForGroup, false);
        } else {
            Entity fromTechnologyGroup = lineChangeoverNormsForOrdersService
                    .getTechnologyGroupByNumberFromDB((String) previousOrderTechnologyGroupNumberField.getFieldValue());
            Entity toTechnologyGroup = lineChangeoverNormsForOrdersService
                    .getTechnologyGroupByNumberFromDB((String) technologyGroupNumberField.getFieldValue());

            if ((changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(fromTechnologyGroup,
                    toTechnologyGroup, productionLine) == null)
                    && (changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(
                            fromTechnologyGroup, toTechnologyGroup, null) == null)) {
                updateButtonState(showChangeoverNormForGroup, false);
            } else {
                updateButtonState(showChangeoverNormForGroup, true);
            }
        }

        if (StringUtils.isEmpty((String) previousOrderTechnologyNumberField.getFieldValue())
                || StringUtils.isEmpty((String) technologyNumberField.getFieldValue())) {
            updateButtonState(showChangeoverNormForTechnology, false);
        } else {
            Entity fromTechnology = lineChangeoverNormsForOrdersService
                    .getTechnologyByNumberFromDB((String) previousOrderTechnologyNumberField.getFieldValue());
            Entity toTechnology = lineChangeoverNormsForOrdersService.getTechnologyByNumberFromDB((String) technologyNumberField
                    .getFieldValue());

            if ((changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                    productionLine) == null)
                    && (changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology,
                            toTechnology, null) == null)) {
                updateButtonState(showChangeoverNormForTechnology, false);
            } else {
                updateButtonState(showChangeoverNormForTechnology, true);
            }
        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void showOwnLineChangeoverDurationField(final ViewDefinitionState view) {
        orderService.changeFieldState(view, OrderFieldsLCNFO.OWN_LINE_CHANGEOVER, OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION);
    }

}
