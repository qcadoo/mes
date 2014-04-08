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
package com.qcadoo.mes.productionCounting;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class ProductionTrackingServiceImpl implements ProductionTrackingService {

    private static final String L_BORDER_LAYOUT_TIME = "borderLayoutTime";

    private static final String L_BORDER_LAYOUT_PIECEWORK = "borderLayoutPiecework";

    private static final String L_DONE_QUANTITY = "doneQuantity";

    private static final String L_AMOUNT_OF_PRODUCT_PRODUCED = "amountOfProductProduced";

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillProductionLineLookup(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference("order");
        Entity order = orderLookup.getEntity();
        Long productionLineId = null;
        if (order != null) {
            productionLineId = order.getBelongsToField(OrderFields.PRODUCTION_LINE).getId();
        }
        LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference("productionLine");
        productionLineLookup.setFieldValue(productionLineId);
    }

    @Override
    public void fillShiftAndDivisionField(final ViewDefinitionState view) {
        LookupComponent staffLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.STAFF);
        LookupComponent shiftLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.SHIFT);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);

        Entity staff = staffLookup.getEntity();

        if (staff == null) {
            shiftLookup.setFieldValue(null);

            return;
        }

        Entity shift = staff.getBelongsToField(ProductionTrackingFields.SHIFT);

        if (shift == null) {
            shiftLookup.setFieldValue(null);
        } else {
            shiftLookup.setFieldValue(shift.getId());
        }

        Entity division = staff.getBelongsToField(ProductionTrackingFields.DIVISION);

        if (division == null) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    @Override
    public void fillDivisionField(final ViewDefinitionState view) {
        LookupComponent workstationTypeLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.WORKSTATION_TYPE);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);

        Entity workstationType = workstationTypeLookup.getEntity();

        if (workstationType == null) {
            divisionLookup.setFieldValue(null);

            return;
        }

        Entity division = workstationType.getBelongsToField(ProductionTrackingFields.DIVISION);

        if (division == null) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    @Override
    public void setTimeAndPieceworkComponentsVisible(final ViewDefinitionState view, final Entity order) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        ComponentState borderLayoutTime = view.getComponentByReference(L_BORDER_LAYOUT_TIME);
        ComponentState borderLayoutPiecework = view.getComponentByReference(L_BORDER_LAYOUT_PIECEWORK);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        boolean registerProductionTime = order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME);
        boolean registerPiecework = order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK);

        boolean isBasic = productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording);
        boolean isForEach = productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording);

        technologyOperationComponentLookup.setEnabled(isForEach);
        technologyOperationComponentLookup.setRequired(isForEach);
        technologyOperationComponentLookup.setVisible(isForEach);

        borderLayoutTime.setVisible(registerProductionTime && !isBasic);
        borderLayoutPiecework.setVisible(registerPiecework && isForEach);
    }

    @Override
    public void setTimeAndPiecworkComponentsVisible(final String recordingType, final Entity order, final ViewDefinitionState view) {
        boolean recordingTypeEqualsForEach = TypeOfProductionRecording.FOR_EACH.getStringValue().equals(recordingType);
        boolean recordingTypeEqualsBasic = TypeOfProductionRecording.BASIC.getStringValue().equals(recordingType);

        view.getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT).setVisible(
                recordingTypeEqualsForEach);

        boolean registerProductionTime = order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME);
        view.getComponentByReference("timeTab").setVisible(registerProductionTime && !recordingTypeEqualsBasic);

        ProductionTrackingState recordState = getTrackingState(view);
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem calcTotalLaborTimeBtn = window.getRibbon().getGroupByName("workTime")
                .getItemByName("calcTotalLaborTime");
        calcTotalLaborTimeBtn.setEnabled(registerProductionTime && !recordingTypeEqualsBasic
                && ProductionTrackingState.DRAFT.equals(recordState));
        calcTotalLaborTimeBtn.requestUpdate(true);

        boolean registerPiecework = order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK);
        view.getComponentByReference("pieceworkTab").setVisible(registerPiecework && recordingTypeEqualsForEach);
    }

    public ProductionTrackingState getTrackingState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity productionRecordFormEntity = form.getEntity();
        String stateStringValue = productionRecordFormEntity.getStringField(ProductionTrackingFields.STATE);
        if (StringUtils.isEmpty(stateStringValue)) {
            return ProductionTrackingState.DRAFT;
        }
        return ProductionTrackingState.parseString(stateStringValue);
    }

    @Override
    public void changeProducedQuantityFieldState(final ViewDefinitionState viewDefinitionState) {
        final FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = null;
        if (form.getEntityId() != null) {
            order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    form.getEntityId());
        }

        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        ComponentState doneQuantity = viewDefinitionState.getComponentByReference(L_DONE_QUANTITY);
        ComponentState amountOfPP = viewDefinitionState.getComponentByReference(L_AMOUNT_OF_PRODUCT_PRODUCED);

        if (order == null || order.getStringField(OrderFields.STATE).equals(OrderState.PENDING.getStringValue())
                || order.getStringField(OrderFields.STATE).equals(OrderState.ACCEPTED.getStringValue())) {
            doneQuantity.setEnabled(false);
            amountOfPP.setEnabled(false);
        } else if ("".equals(typeOfProductionRecording.getFieldValue())
                || TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording.getFieldValue())) {
            doneQuantity.setEnabled(true);
            amountOfPP.setEnabled(true);
        } else {
            doneQuantity.setEnabled(false);
            amountOfPP.setEnabled(false);
        }
    }

}
