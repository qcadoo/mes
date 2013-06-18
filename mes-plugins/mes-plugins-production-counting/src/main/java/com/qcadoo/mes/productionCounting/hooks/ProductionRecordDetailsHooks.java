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
import com.qcadoo.mes.productionCounting.ProductionRecordService;
import com.qcadoo.mes.productionCounting.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateStringValues;
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
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductionRecordDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_ACTIONS = "actions";

    private static final String L_COPY = "copy";

    private static final String L_IS_DISABLED = "isDisabled";

    private List<String> productionRecordFieldNames = Lists.newArrayList(ProductionRecordFields.LAST_RECORD,
            ProductionRecordFields.NUMBER, ProductionRecordFields.ORDER, ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT,
            ProductionRecordFields.STAFF, ProductionRecordFields.SHIFT, ProductionRecordFields.WORKSTATION_TYPE,
            ProductionRecordFields.DIVISION, ProductionRecordFields.LABOR_TIME, ProductionRecordFields.MACHINE_TIME,
            ProductionRecordFields.EXECUTED_OPERATION_CYCLES);

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionRecordService productionRecordService;

    public void initializeRecordDetailsView(final ViewDefinitionState view) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent stateField = (FieldComponent) view.getComponentByReference(ProductionRecordFields.STATE);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionRecordFields.ORDER);
        FieldComponent isDisabledField = (FieldComponent) view.getComponentByReference(L_IS_DISABLED);

        if (productionRecordForm.getEntityId() == null) {
            stateField.setFieldValue(ProductionRecordStateStringValues.DRAFT);
            stateField.requestComponentUpdateState();

            return;
        }

        Entity productionRecord = productionRecordForm.getEntity();

        stateField.setFieldValue(productionRecord.getField(ProductionRecordFields.STATE));
        stateField.requestComponentUpdateState();

        Entity order = orderLookup.getEntity();

        isDisabledField.setFieldValue(false);

        productionRecordService.setTimeAndPieceworkComponentsVisible(view, order);
    }

    public void disabledFieldWhenStateNotDraft(final ViewDefinitionState view) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (productionRecordForm.getEntityId() == null) {
            return;
        }

        Entity productionRecord = productionRecordForm.getEntity();

        String state = productionRecord.getStringField(ProductionRecordFields.STATE);

        boolean isDraft = (ProductionRecordStateStringValues.DRAFT.equals(state));

        setFieldComponentsEnabledAndGridsEditable(view, isDraft);
    }

    private void setFieldComponentsEnabledAndGridsEditable(final ViewDefinitionState view, final boolean isEnabled) {
        productionCountingService.setComponentsState(view, productionRecordFieldNames, isEnabled, true);

        GridComponent recordOperationProductInComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS);

        GridComponent recordOperationProductOutComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS);
        GridComponent stateChangesGrid = (GridComponent) view.getComponentByReference(ProductionRecordFields.STATE_CHANGES);

        recordOperationProductInComponentsGrid.setEditable(isEnabled);
        recordOperationProductOutComponentsGrid.setEditable(isEnabled);
        stateChangesGrid.setEditable(isEnabled);
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup actions = window.getRibbon().getGroupByName(L_ACTIONS);
        RibbonActionItem copy = actions.getItemByName(L_COPY);

        if (productionRecordForm.getEntityId() == null) {
            return;
        }

        Entity productionRecord = productionRecordForm.getEntity();

        Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);

        if (order == null) {
            return;
        }

        String state = order.getStringField(OrderFields.STATE);

        boolean isInProgress = OrderStateStringValues.IN_PROGRESS.equals(state);

        copy.setEnabled(isInProgress);
        copy.requestUpdate(true);
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        GridComponent stateChangesGrid = (GridComponent) view.getComponentByReference(ProductionRecordFields.STATE_CHANGES);

        CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                OrderStateChangeFields.STATUS, Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));

        stateChangesGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void fillShiftAndDivisionField(final ViewDefinitionState view) {
        productionRecordService.fillShiftAndDivisionField(view);
    }

    public void fillDivisionField(final ViewDefinitionState view) {
        productionRecordService.fillDivisionField(view);
    }

}
