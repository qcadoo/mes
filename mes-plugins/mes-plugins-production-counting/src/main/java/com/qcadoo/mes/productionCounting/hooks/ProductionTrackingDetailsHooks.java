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
package com.qcadoo.mes.productionCounting.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductionTrackingDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_ACTIONS = "actions";

    private static final String L_PRODUCTS_QUANTITIES = "productsQuantities";

    private static final String L_COPY = "copy";

    private static final String L_COPY_PLANNED_QUANTITY_TO_USED_QUANTITY = "copyPlannedQuantityToUsedQuantity";

    private static final String L_IS_DISABLED = "isDisabled";

    private static final List<String> L_PRODUCTION_TRACKING_FIELD_NAMES = Lists.newArrayList(
            ProductionTrackingFields.LAST_TRACKING, ProductionTrackingFields.NUMBER, ProductionTrackingFields.ORDER,
            ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, ProductionTrackingFields.STAFF,
            ProductionTrackingFields.SHIFT, ProductionTrackingFields.WORKSTATION_TYPE, ProductionTrackingFields.DIVISION,
            ProductionTrackingFields.LABOR_TIME, ProductionTrackingFields.MACHINE_TIME,
            ProductionTrackingFields.EXECUTED_OPERATION_CYCLES);

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity productionTracking = productionTrackingForm.getEntity();

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        if (order != null) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (technology != null) {
                FilterValueHolder filterValueHolder = technologyOperationComponentLookup.getFilterValue();
                filterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                technologyOperationComponentLookup.setFilterValue(filterValueHolder);
            }
        }
    }

    public void initializeTrackingDetailsView(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent stateField = (FieldComponent) view.getComponentByReference(ProductionTrackingFields.STATE);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.ORDER);
        FieldComponent isDisabledField = (FieldComponent) view.getComponentByReference(L_IS_DISABLED);

        if (productionTrackingForm.getEntityId() == null) {
            stateField.setFieldValue(ProductionTrackingStateStringValues.DRAFT);
            stateField.requestComponentUpdateState();

            return;
        }

        Entity productionTracking = productionTrackingForm.getEntity();

        stateField.setFieldValue(productionTracking.getField(ProductionTrackingFields.STATE));
        stateField.requestComponentUpdateState();

        Entity order = orderLookup.getEntity();

        isDisabledField.setFieldValue(false);

        productionTrackingService.setTimeAndPieceworkComponentsVisible(view, order);
    }

    public void disableFieldsWhenStateNotDraft(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (productionTrackingForm.getEntity() == null) {
            return;
        }

        Entity productionTracking = productionTrackingForm.getEntity();

        String state = productionTracking.getStringField(ProductionTrackingFields.STATE);

        boolean isDraft = (ProductionTrackingStateStringValues.DRAFT.equals(state));

        setFieldComponentsEnabledAndGridsEditable(view, isDraft);
    }

    private void setFieldComponentsEnabledAndGridsEditable(final ViewDefinitionState view, final boolean isEnabled) {
        productionCountingService.setComponentsState(view, L_PRODUCTION_TRACKING_FIELD_NAMES, isEnabled, true);

        GridComponent trackingOperationProductInComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        GridComponent trackingOperationProductOutComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
        GridComponent stateChangesGrid = (GridComponent) view.getComponentByReference(ProductionTrackingFields.STATE_CHANGES);

        trackingOperationProductInComponentsGrid.setEditable(isEnabled);
        trackingOperationProductOutComponentsGrid.setEditable(isEnabled);
        stateChangesGrid.setEditable(isEnabled);
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup actions = window.getRibbon().getGroupByName(L_ACTIONS);
        RibbonGroup productsQuantities = window.getRibbon().getGroupByName(L_PRODUCTS_QUANTITIES);

        RibbonActionItem copy = actions.getItemByName(L_COPY);
        RibbonActionItem copyPlannedQuantityToUsedQuantity = productsQuantities
                .getItemByName(L_COPY_PLANNED_QUANTITY_TO_USED_QUANTITY);

        if (productionTrackingForm.getEntityId() == null) {
            return;
        }

        Entity productionTracking = productionTrackingForm.getEntity();
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        if (order == null) {
            return;
        }

        String state = productionTracking.getStringField(ProductionTrackingFields.STATE);
        String orderState = order.getStringField(OrderFields.STATE);

        boolean isInProgress = OrderStateStringValues.IN_PROGRESS.equals(orderState);
        boolean isDraft = ProductionTrackingStateStringValues.DRAFT.equals(state);

        copy.setEnabled(isInProgress);
        copy.requestUpdate(true);

        copyPlannedQuantityToUsedQuantity.setEnabled(isDraft);
        copyPlannedQuantityToUsedQuantity.requestUpdate(true);
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        GridComponent stateChangesGrid = (GridComponent) view.getComponentByReference(ProductionTrackingFields.STATE_CHANGES);

        CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                OrderStateChangeFields.STATUS, Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));

        stateChangesGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void fillShiftAndDivisionField(final ViewDefinitionState view) {
        productionTrackingService.fillShiftAndDivisionField(view);
    }

    public void fillDivisionField(final ViewDefinitionState view) {
        productionTrackingService.fillDivisionField(view);
    }

}
