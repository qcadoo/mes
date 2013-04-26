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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class RecordOperationProductInComponentDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void fillPlannedQuantity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        if (form.getEntity() == null) {
            return;
        }

        Entity recordOperationProductInComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT).get(form.getEntityId());

        // Entity productionCountingQuantity = recordOperationProductInComponent.getBelongsToField(PRODUCTION_COUNTING_QUANTITY);
        //
        // if (productionCountingQuantity == null) {
        // return;
        // }
        //
        // FieldComponent plannedQuantityField = (FieldComponent) view.getComponentByReference(PLANNED_QUANTITY);
        // plannedQuantityField.setFieldValue(numberService.format(productionCountingQuantity.getDecimalField(PLANNED_QUANTITY)));
        // plannedQuantityField.requestComponentUpdateState();
    }

}
