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
package com.qcadoo.mes.technologies.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OPICDetailsHooks {

    @Autowired
    private UnitService unitService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillUnitBeforeRender(view);
        setProductBySizeGroupsGridEnabledAndClear(view);
    }

    private void fillUnitBeforeRender(final ViewDefinitionState view) {
        unitService.fillProductUnitBeforeRenderIfEmpty(view, OperationProductInComponentFields.UNIT);
        unitService.fillProductUnitBeforeRenderIfEmpty(view, OperationProductInComponentFields.GIVEN_UNIT);
    }

    private void setProductBySizeGroupsGridEnabledAndClear(final ViewDefinitionState view) {
        FormComponent operationProductInComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent differentProductsInDifferentSizesCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OperationProductInComponentFields.PRODUCT);
        GridComponent productBySizeGroupsGrid = (GridComponent) view
                .getComponentByReference(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS);

        Entity operationProductInComponent = operationProductInComponentForm.getEntity();
        Entity operationComponent = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);
        Entity technology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);
        String entityType = product.getStringField(ProductFields.ENTITY_TYPE);

        boolean isEnabled = ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(entityType);
        boolean isChecked = differentProductsInDifferentSizesCheckBox.isChecked();

        if (isChecked) {
            productLookup.setFieldValue(null);
        } else {
            productBySizeGroupsGrid.setEntities(Lists.newArrayList());
        }

        differentProductsInDifferentSizesCheckBox.setEnabled(isEnabled);
        differentProductsInDifferentSizesCheckBox.requestComponentUpdateState();
        productLookup.setEnabled(!isChecked);
        productLookup.requestComponentUpdateState();
        productBySizeGroupsGrid.setEnabled(isChecked);
    }

}
