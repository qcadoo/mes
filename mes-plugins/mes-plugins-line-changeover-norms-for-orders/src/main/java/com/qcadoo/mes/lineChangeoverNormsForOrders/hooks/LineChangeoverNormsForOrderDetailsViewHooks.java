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

import com.google.common.collect.Lists;
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
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LineChangeoverNormsForOrderDetailsViewHooks {

    private static final String L_ACTIONS = "actions";

    private static final String L_SAVE_OWN_TIME = "saveOwnTime";

    private static final String L_ORDERS = "orders";

    private static final String L_SHOW_PREVIOUS_ORDER = "showPreviousOrder";

    private static final String L_LINE_CHANGEOVER_NORMS = "lineChangeoverNorms";

    private static final String L_SHOW_BEST_FITTING_LINE_CHANGEOVER_NORM = "showBestFittingLineChangeoverNorm";

    private static final String L_SHOW_LINE_CHANGEOVER_NORM_FOR_GROUP = "showLineChangeoverNormForGroup";

    private static final String L_SHOW_LINE_CHANGEOVER_NORM_FOR_TECHNOLOGY = "showLineChangeoverNormForTechnology";

    private static final String L_PREVIOUS_ORDER_TECHNOLOGY_GROUP_NUMBER = "previousOrderTechnologyGroupNumber";

    private static final String L_TECHNOLOGY_GROUP_NUMBER = "technologyGroupNumber";

    private static final String L_PREVIOUS_ORDER_TECHNOLOGY_NUMBER = "previousOrderTechnologyNumber";

    private static final String L_TECHNOLOGY_NUMBER = "technologyNumber";

    @Autowired
    private OrderService orderService;

    @Autowired
    private ChangeoverNormsService changeoverNormsService;

    @Autowired
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        fillOrderForms(view);
        fillLineChangeoverNorm(view);
        showOwnLineChangeoverDurationField(view);
        updateRibbonState(view);
    }

    public void fillOrderForms(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OrderFieldsLCNFO.ORDER);
        LookupComponent previousOrderLookup = (LookupComponent) view.getComponentByReference(OrderFieldsLCNFO.PREVIOUS_ORDER);

        if (Objects.isNull(orderForm.getEntityId())) {
            return;
        }

        Entity order = orderForm.getPersistedEntityWithIncludedFormValues();

        orderLookup.setFieldValue(order.getId());
        orderLookup.requestComponentUpdateState();

        lineChangeoverNormsForOrdersService.fillOrderForm(view, LineChangeoverNormsForOrdersConstants.ORDER_FIELDS);

        if (previousOrderLookup.isEmpty()) {
            Entity previousOrder = lineChangeoverNormsForOrdersService.getPreviousOrderFromDB(order);

            if (Objects.nonNull(previousOrder)) {
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

        if (Objects.isNull(order)) {
            return null;
        }

        Entity fromTechnology = extractTechnologyFrom(previousOrderLookup.getEntity());
        Entity toTechnology = extractTechnologyFrom(order);

        if (Objects.isNull(fromTechnology) || Objects.isNull(toTechnology)) {
            return null;
        }

        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        return changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
    }

    private Entity extractTechnologyFrom(final Entity order) {
        if (Objects.isNull(order)) {
            return null;
        }

        return order.getBelongsToField(OrderFields.TECHNOLOGY);
    }

    private void fillChangeoverNormFields(final ViewDefinitionState view, final Entity lineChangeoverNorm) {
        FieldComponent lineChangeoverNormField = (FieldComponent) view.getComponentByReference("lineChangeoverNorm");
        FieldComponent lineChangeoverNormDurationField = (FieldComponent) view
                .getComponentByReference("lineChangeoverNormDuration");

        if (Objects.isNull(lineChangeoverNorm)) {
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
                .getComponentByReference(L_PREVIOUS_ORDER_TECHNOLOGY_GROUP_NUMBER);
        FieldComponent technologyGroupNumberField = (FieldComponent) view.getComponentByReference(L_TECHNOLOGY_GROUP_NUMBER);
        FieldComponent previousOrderTechnologyNumberField = (FieldComponent) view
                .getComponentByReference(L_PREVIOUS_ORDER_TECHNOLOGY_NUMBER);
        FieldComponent technologyNumberField = (FieldComponent) view.getComponentByReference(L_TECHNOLOGY_NUMBER);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup actions = window.getRibbon().getGroupByName(L_ACTIONS);
        RibbonActionItem saveOwnTime = actions.getItemByName(L_SAVE_OWN_TIME);

        RibbonGroup orders = window.getRibbon().getGroupByName(L_ORDERS);
        RibbonActionItem showPreviousOrder = orders.getItemByName(L_SHOW_PREVIOUS_ORDER);

        RibbonGroup lineChangeoverNorms = window.getRibbon().getGroupByName(L_LINE_CHANGEOVER_NORMS);
        RibbonActionItem showBestFittingLineChangeoverNorm = lineChangeoverNorms
                .getItemByName(L_SHOW_BEST_FITTING_LINE_CHANGEOVER_NORM);
        RibbonActionItem showChangeoverNormForGroup = lineChangeoverNorms.getItemByName(L_SHOW_LINE_CHANGEOVER_NORM_FOR_GROUP);
        RibbonActionItem showChangeoverNormForTechnology = lineChangeoverNorms
                .getItemByName(L_SHOW_LINE_CHANGEOVER_NORM_FOR_TECHNOLOGY);

        Long productionLineId = (Long) productionLineField
                .getFieldValue();

        Entity productionLine = null;

        if (Objects.nonNull(productionLineId)) {
            productionLine = lineChangeoverNormsForOrdersService.getProductionLineFromDB(productionLineId);
        }

        boolean hasProductionTrackingsList = !view.getJsonContext().has("window.activeMenu");
        updateButtonState(saveOwnTime, hasProductionTrackingsList);

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

            boolean isEnabled = Objects.nonNull(changeoverNormsSearchService.searchMatchingChangeoverNormsForTechnologyGroupWithLine(fromTechnologyGroup,
                    toTechnologyGroup, productionLine))
                    || Objects.nonNull(changeoverNormsSearchService.searchMatchingChangeoverNormsForTechnologyGroupWithLine(
                    fromTechnologyGroup, toTechnologyGroup, null));

            updateButtonState(showChangeoverNormForGroup, isEnabled);
        }

        if (StringUtils.isEmpty((String) previousOrderTechnologyNumberField.getFieldValue())
                || StringUtils.isEmpty((String) technologyNumberField.getFieldValue())) {
            updateButtonState(showChangeoverNormForTechnology, false);
        } else {
            Entity fromTechnology = lineChangeoverNormsForOrdersService
                    .getTechnologyByNumberFromDB((String) previousOrderTechnologyNumberField.getFieldValue());
            Entity toTechnology = lineChangeoverNormsForOrdersService.getTechnologyByNumberFromDB((String) technologyNumberField
                    .getFieldValue());

            boolean isEnabled = Objects.nonNull(changeoverNormsSearchService.searchMatchingChangeoverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                    productionLine))
                    || Objects.nonNull(changeoverNormsSearchService.searchMatchingChangeoverNormsForTechnologyWithLine(fromTechnology,
                    toTechnology, null));

            updateButtonState(showChangeoverNormForTechnology, isEnabled);
        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void showOwnLineChangeoverDurationField(final ViewDefinitionState view) {
        if (view.getJsonContext().has("window.activeMenu")) {
            Lists.newArrayList(OrderFieldsLCNFO.OWN_LINE_CHANGEOVER, OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION).forEach(fieldName -> {
                FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldName);
                fieldComponent.setEnabled(false);
            });
        } else {
            orderService.changeFieldState(view, OrderFieldsLCNFO.OWN_LINE_CHANGEOVER, OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION);
        }
    }

}
