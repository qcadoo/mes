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
package com.qcadoo.mes.basicProductionCounting.listeners;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class  ProductionCountingQuantityAdvancedDetailsListeners {

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUnit";

    private static final String L_USED_QUANTITY_UNIT = "usedQuantityUnit";

    private static final String L_PRODUCED_QUANTITY_UNIT = "producedQuantityUnit";

    private static final String L_SAVE = "save";

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void saveProductionCountingQuantity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, L_SAVE, args);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity productionCountingQuantity = form.getEntity();
        fillOrderAdditionalProductField(productionCountingQuantity);
        afterSave(productionCountingQuantity);
    }

    //override by aspect !!!
    public void afterSave(Entity productionCountingQuantity) {

    }
    private void fillOrderAdditionalProductField(Entity productionCountingQuantity) {

        if(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL)
                .equals(ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue())) {
            Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
            String additionalFinalProducts = order.getStringField(OrderFields.ADDITIONAL_FINAL_PRODUCTS);

            if(!Strings.isNullOrEmpty(additionalFinalProducts)) {
                additionalFinalProducts = additionalFinalProducts + "\n";
            } else {
                additionalFinalProducts = "";
            }

            Entity p = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            additionalFinalProducts = String.format("%s%s - %s", additionalFinalProducts, p.getStringField(ProductFields.NUMBER), p.getStringField(ProductFields.NAME));
            order.setField(OrderFields.ADDITIONAL_FINAL_PRODUCTS, additionalFinalProducts);
            order.getDataDefinition().fastSave(order);
        }

    }
    public void fillUnitFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<String> referenceNames = Lists.newArrayList(L_PLANNED_QUANTITY_UNIT, L_USED_QUANTITY_UNIT, L_PRODUCED_QUANTITY_UNIT);

        basicProductionCountingService.fillUnitFields(view, ProductionCountingQuantityFields.PRODUCT, referenceNames);
    }

    public void setTechnologyOperationComponentFieldRequired(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        basicProductionCountingService.setTechnologyOperationComponentFieldRequired(view);
    }

}
