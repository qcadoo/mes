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

import static com.qcadoo.mes.productionCounting.internal.constants.RecordOperationProductInComponentFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.RecordOperationProductInComponentFields.USED_QUANTITY;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionRecordDetailsListeners {

    private static final String L_FORM = "form";

    @Autowired
    private NumberService numberService;

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
            BigDecimal plannedQuantity = recordOperationProductComponent.getDecimalField(PLANNED_QUANTITY);

            if (plannedQuantity == null) {
                plannedQuantity = BigDecimal.ZERO;
            }

            recordOperationProductComponent.setField(USED_QUANTITY, numberService.setScale(plannedQuantity));

            recordOperationProductComponent.getDataDefinition().save(recordOperationProductComponent);
        }
    }

}
