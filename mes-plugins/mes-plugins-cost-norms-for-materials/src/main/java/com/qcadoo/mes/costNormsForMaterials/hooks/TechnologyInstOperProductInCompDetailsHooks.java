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
package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants.CURRENCY_FIELDS_ORDER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costNormsForProduct.hooks.ProductDetailsHooksCNFP;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class TechnologyInstOperProductInCompDetailsHooks {

    @Autowired
    private ProductDetailsHooksCNFP costNormsForProductService;

    private static final String L_COST_FOR_NUMBER_UNIT = "costForNumberUnit";

    public void fillUnitField(final ViewDefinitionState viewDefinitionState) {
        FieldComponent costForNumberUnit = (FieldComponent) viewDefinitionState.getComponentByReference(L_COST_FOR_NUMBER_UNIT);
        LookupComponent productLookup = (LookupComponent) viewDefinitionState.getComponentByReference("product");
        Entity product = productLookup.getEntity();
        if (product == null) {
            return;
        }
        String unit = product.getStringField(ProductFields.UNIT);
        costForNumberUnit.setFieldValue(unit);
        costForNumberUnit.requestComponentUpdateState();
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        costNormsForProductService.fillCurrencyFields(viewDefinitionState, CURRENCY_FIELDS_ORDER);
    }
}
