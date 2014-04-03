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
package com.qcadoo.mes.productionCounting.listeners;

import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.*;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.*;
import static com.qcadoo.mes.productionCounting.internal.constants.RecordOperationProductInComponentFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.RecordOperationProductInComponentFields.USED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.ProductionRecordViewService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.utils.StaffTimeCalculator;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionRecordDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_RECORD_OPERATION_PRODUCT_IN_COMPONENT = "recordOperationProductInComponent";

    private static final String L_RECORD_OPERATION_PRODUCT_OUT_COMPONENT = "recordOperationProductOutComponent";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionRecordDetailsListeners.class);

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionRecordViewService productionRecordViewService;

    @Autowired
    private StaffTimeCalculator staffTimeCalculator;

    public void calcTotalLaborTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Long id = form.getEntityId();
        if (id == null) {
            return;
        }
        Long totalLabor = staffTimeCalculator.countTotalLaborTime(id);
        FieldComponent laborTimeInput = (FieldComponent) view.getComponentByReference("laborTime");
        laborTimeInput.setFieldValue(totalLabor);
    }

    public void copyPlannedQuantityToUsedQuantity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long productionRecordId = productionRecordForm.getEntityId();

        if (productionRecordId == null) {
            return;
        }

        Entity productionRecord = productionRecordForm.getEntity().getDataDefinition().get(productionRecordId);

        copyPlannedQuantityToUsedQuantity(productionRecord
                .getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS));
        copyPlannedQuantityToUsedQuantity(productionRecord
                .getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS));
    }

    private void copyPlannedQuantityToUsedQuantity(List<Entity> recordOperationProductComponents) {
        for (Entity recordOperationProductComponent : recordOperationProductComponents) {
            BigDecimal plannedQuantity = BigDecimalUtils.convertNullToZero(recordOperationProductComponent
                    .getDecimalField(PLANNED_QUANTITY));
            recordOperationProductComponent.setField(USED_QUANTITY, numberService.setScale(plannedQuantity));
            recordOperationProductComponent.getDataDefinition().save(recordOperationProductComponent);
        }
    }

    public void disableFields(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        productionRecordViewService.changeProducedQuantityFieldState(viewDefinitionState);
        Object recordingTypeValue = ((FieldComponent) viewDefinitionState.getComponentByReference(TYPE_OF_PRODUCTION_RECORDING))
                .getFieldValue();
        boolean recordingTypeEqualsCumulated = CUMULATED.getStringValue().equals(recordingTypeValue);
        boolean recordingTypeEqualsForEach = FOR_EACH.getStringValue().equals(recordingTypeValue);
        if (recordingTypeEqualsCumulated || recordingTypeEqualsForEach) {
            for (String componentName : Arrays.asList(REGISTER_QUANTITY_IN_PRODUCT, REGISTER_QUANTITY_OUT_PRODUCT,
                    REGISTER_PRODUCTION_TIME, JUST_ONE, ALLOW_TO_CLOSE, AUTO_CLOSE_ORDER, REGISTER_PIECEWORK)) {
                ComponentState component = viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(true);
            }
        }
    }

    public void enabledOrDisableFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("order is null");
            }
            return;
        }

        String recordingType = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(recordingType, order, view);
    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference(ORDER);
        return lookup.getEntity();
    }

    public void checkJustOne(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            return;
        }
        FieldComponent lastRecord = (FieldComponent) view.getComponentByReference(LAST_RECORD);
        boolean justOneRecord = order.getBooleanField(JUST_ONE);
        lastRecord.setFieldValue(justOneRecord);
        lastRecord.setEnabled(!justOneRecord);
        lastRecord.requestComponentUpdateState();
    }

    public void clearFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FieldComponent operation = (FieldComponent) view.getComponentByReference(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
        operation.setFieldValue("");
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        GridComponent productsIn = (GridComponent) view.getComponentByReference(L_RECORD_OPERATION_PRODUCT_IN_COMPONENT);
        GridComponent productOut = (GridComponent) view.getComponentByReference(L_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);

        productOut.setEntities(new ArrayList<Entity>());
        productsIn.setEntities(new ArrayList<Entity>());
    }

}
