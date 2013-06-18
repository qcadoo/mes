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

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionCounting.ProductionRecordService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionRecordFields;
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

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_USED_QUANTITY = "usedQuantity";

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionRecordService productionRecordService;

    public void fillShiftAndDivisionField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        productionRecordService.fillShiftAndDivisionField(view);
    }

    public final void fillDivisionField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        productionRecordService.fillDivisionField(view);
    }

    public void copyPlannedQuantityToUsedQuantity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (productionRecordForm.getEntityId() == null) {
            return;
        }

        Entity productionRecord = productionRecordForm.getEntity();

        copyPlannedQuantityToUsedQuantity(productionRecord
                .getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS));
        copyPlannedQuantityToUsedQuantity(productionRecord
                .getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS));
    }

    private void copyPlannedQuantityToUsedQuantity(List<Entity> recordOperationProductComponents) {
        for (Entity recordOperationProductComponent : recordOperationProductComponents) {
            BigDecimal plannedQuantity = recordOperationProductComponent.getDecimalField(L_PLANNED_QUANTITY);

            if (plannedQuantity == null) {
                plannedQuantity = BigDecimal.ZERO;
            }

            recordOperationProductComponent.setField(L_USED_QUANTITY, numberService.setScale(plannedQuantity));

            recordOperationProductComponent.getDataDefinition().save(recordOperationProductComponent);
        }
    }

    public void clearFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT);

        technologyOperationComponentLookup.setFieldValue(null);

        if (productionRecordForm.getEntityId() == null) {
            return;
        }

        GridComponent recordOperationProductInComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS);
        GridComponent recordOperationProductOutComponentGrid = (GridComponent) view
                .getComponentByReference(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS);

        List<Entity> emptyList = Lists.newArrayList();

        recordOperationProductOutComponentGrid.setEntities(emptyList);
        recordOperationProductInComponentsGrid.setEntities(emptyList);
    }

    public void enabledOrDisableFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionRecordFields.ORDER);
        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        productionRecordService.setTimeAndPieceworkComponentsVisible(view, order);
    }

    public void checkJustOne(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FieldComponent lastRecord = (FieldComponent) view.getComponentByReference(ProductionRecordFields.LAST_RECORD);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionRecordFields.ORDER);
        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        boolean isJustOne = order.getBooleanField(OrderFieldsPC.JUST_ONE);

        lastRecord.setFieldValue(isJustOne);
        lastRecord.setEnabled(!isJustOne);
        lastRecord.requestComponentUpdateState();
    }

}
