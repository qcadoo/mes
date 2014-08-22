/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.basicProductionCounting.hooks;

import java.util.Arrays;

import com.qcadoo.mes.basicProductionCounting.hooks.util.ProductionProgressModifyLockHelper;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.view.api.components.GridComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class BasicProductionCountingDetailsHooks {

    private static final String L_GRID = "productionCountingQuantities";

    private static final String L_FORM = "form";

    private static final String L_BASIC = "01basic";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUnit";

    private static final String L_USED_QUANTITY_UNIT = "usedQuantityUnit";

    private static final String L_PRODUCED_QUANTITY_UNIT = "producedQuantityUnit";

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductionProgressModifyLockHelper progressModifyLockHelper;

    public void setFieldEditableDependsOfOrderState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        Long formId = form.getEntityId();
        if (formId == null) {
            return;
        }
        Entity basicProductionCounting = form.getEntity();

        boolean isLocked = progressModifyLockHelper.isLocked(orderService.getOrder( basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER).getId()));
        grid.setEnabled(!isLocked);
    }

    public void disableUsedAndProducedFieldsDependsOfProductType(final ViewDefinitionState view) {
        FormComponent basicProductionCountingForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent producedQuantityField = (FieldComponent) view
                .getComponentByReference(BasicProductionCountingFields.PRODUCED_QUANTITY);
        FieldComponent usedQuantityField = (FieldComponent) view
                .getComponentByReference(BasicProductionCountingFields.USED_QUANTITY);

        Long basicProductionCountingId = basicProductionCountingForm.getEntityId();

        if (basicProductionCountingId != null) {
            Entity basicProductionCounting = basicProductionCountingService.getBasicProductionCounting(basicProductionCountingId);

            Entity product = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.PRODUCT);
            Entity order = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER);
            String state = order.getStringField(OrderFields.STATE);
            String typeOfProductionRecording = order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);

            if (L_BASIC.equals(typeOfProductionRecording)
                    && (OrderStateStringValues.IN_PROGRESS.equals(state) || OrderStateStringValues.INTERRUPTED.equals(state))) {
                boolean isUsed = checkIfProductIsUsed(order, product);
                boolean isProduced = checkIfProductIsProduced(order, product);

                usedQuantityField.setEnabled(isUsed);
                producedQuantityField.setEnabled(isProduced);
            } else {
                usedQuantityField.setEnabled(false);
                producedQuantityField.setEnabled(false);
            }
        }
    }

    private boolean checkIfProductIsUsed(final Entity order, final Entity product) {
        return (order
                .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue())).list().getTotalNumberOfEntities() > 0);
    }

    private boolean checkIfProductIsProduced(final Entity order, final Entity product) {
        return (order
                .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue())).list().getTotalNumberOfEntities() > 0);
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        FormComponent basicProductionCountingForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long basicProductionCountingId = basicProductionCountingForm.getEntityId();

        if (basicProductionCountingId == null) {
            return;
        }

        Entity basicProductionCountingEntity = basicProductionCountingService
                .getBasicProductionCounting(basicProductionCountingId);

        Entity product = basicProductionCountingEntity.getBelongsToField(BasicProductionCountingFields.PRODUCT);

        if (product == null) {
            return;
        }

        for (String reference : Arrays.asList(L_PLANNED_QUANTITY_UNIT, L_USED_QUANTITY_UNIT, L_PRODUCED_QUANTITY_UNIT)) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(product.getField(ProductFields.UNIT));
            field.requestComponentUpdateState();
        }
    }

}
