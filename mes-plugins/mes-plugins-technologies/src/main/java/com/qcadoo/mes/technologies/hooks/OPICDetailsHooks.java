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

import java.math.BigDecimal;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ParameterFieldsT;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OPICDetailsHooks {

    @Autowired
    private UnitService unitService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillUnitBeforeRender(view);
        setProductBySizeGroupsGridEnabledAndClear(view);
        setStateForVariousQuantitiesInProductsBySize(view);
        setAttributeTabState(view);
        setOPICDefaultQuantityFromParameter(view);
    }

    private void setOPICDefaultQuantityFromParameter(final ViewDefinitionState view) {
        FormComponent operationProductInComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent quantityField = (FieldComponent) view
                .getComponentByReference(OperationProductInComponentFields.GIVEN_QUANTITY);
        if (operationProductInComponentForm.getEntityId() == null && quantityField.getFieldValue() == null) {
            BigDecimal operationProductInDefaultQuantity = parameterService.getParameter().getDecimalField(ParameterFieldsT.OPERATION_PRODUCT_IN_DEFAULT_QUANTITY);
            if (operationProductInDefaultQuantity != null) {
                quantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(operationProductInDefaultQuantity, 0));
            }
        }
    }

    private void setAttributeTabState(ViewDefinitionState view) {
        FormComponent operationProductInComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent attributeLookup = (LookupComponent) view.getComponentByReference(OperationProductInComponentFields.ATTRIBUTE);

        if (Objects.isNull(attributeLookup)) {
            return;
        }

        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OperationProductInComponentFields.PRODUCT);

        Entity operationProductInComponent = operationProductInComponentForm.getEntity();
        Entity operationComponent = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);
        Entity technology;
        if (Objects.isNull(operationComponent)) {
            technology = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.TECHNOLOGY);
        } else {
            technology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        }
        Entity technologyProduct = technology.getBelongsToField(TechnologyFields.PRODUCT);
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(
                technologyProduct.getField(ProductFields.ENTITY_TYPE)) && Objects.nonNull(productLookup.getEntity())
                && ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()
                .equals(productLookup.getEntity().getField(ProductFields.ENTITY_TYPE))) {
            attributeLookup.setEnabled(true);
        } else {
            attributeLookup.setFieldValue(null);
            attributeLookup.setEnabled(false);
        }
    }

    private void setStateForVariousQuantitiesInProductsBySize(ViewDefinitionState view) {
        CheckBoxComponent differentProductsInDifferentSizesCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);

        CheckBoxComponent variousQuantitiesInProductsBySizeCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE);

        if (differentProductsInDifferentSizesCheckBox.isChecked()) {
            variousQuantitiesInProductsBySizeCheckBox.setEnabled(Boolean.TRUE);
            FieldComponent quantityField = (FieldComponent) view
                    .getComponentByReference(OperationProductInComponentFields.QUANTITY);
            FieldComponent unitField = (FieldComponent) view.getComponentByReference(OperationProductInComponentFields.UNIT);
            FieldComponent quantityFormulaField = (FieldComponent) view
                    .getComponentByReference(OperationProductInComponentFields.QUANTITY_FORMULA);
            FieldComponent givenQuantityField = (FieldComponent) view
                    .getComponentByReference(OperationProductInComponentFields.GIVEN_QUANTITY);
            FieldComponent givenUnitField = (FieldComponent) view
                    .getComponentByReference(OperationProductInComponentFields.GIVEN_UNIT);
            if (variousQuantitiesInProductsBySizeCheckBox.isChecked()) {
                quantityField.setFieldValue(null);
                if (Objects.nonNull(quantityFormulaField)) {
                    quantityFormulaField.setFieldValue(null);
                    quantityFormulaField.setEnabled(false);
                }
                unitField.setFieldValue(null);
                givenQuantityField.setFieldValue(null);
                givenUnitField.setFieldValue(null);
                givenQuantityField.setEnabled(false);
                givenUnitField.setEnabled(false);
            } else {
                if (Objects.nonNull(quantityFormulaField)) {
                    quantityFormulaField.setEnabled(true);
                }
                givenQuantityField.setEnabled(true);
                givenUnitField.setEnabled(true);
            }
        } else {
            variousQuantitiesInProductsBySizeCheckBox.setEnabled(Boolean.FALSE);

        }
    }

    private void fillUnitBeforeRender(final ViewDefinitionState view) {
        unitService.fillProductUnitBeforeRenderIfEmpty(view, OperationProductInComponentFields.UNIT);
        unitService.fillProductUnitBeforeRenderIfEmpty(view, OperationProductInComponentFields.GIVEN_UNIT);
    }

    private void setProductBySizeGroupsGridEnabledAndClear(final ViewDefinitionState view) {
        FormComponent operationProductInComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent differentProductsInDifferentSizesCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);
        CheckBoxComponent variousQuantitiesInProductsBySize = (CheckBoxComponent) view
                .getComponentByReference(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE);
        FieldComponent givenQuantity = (FieldComponent) view
                .getComponentByReference(OperationProductInComponentFields.GIVEN_QUANTITY);
        FieldComponent givenQuantityUnit = (FieldComponent) view
                .getComponentByReference(OperationProductInComponentFields.GIVEN_UNIT);

        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OperationProductInComponentFields.PRODUCT);
        GridComponent productBySizeGroupsGrid = (GridComponent) view
                .getComponentByReference(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS);

        Entity operationProductInComponent = operationProductInComponentForm.getEntity();
        Entity operationComponent = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);
        Entity technology;
        if (Objects.isNull(operationComponent)) {
            technology = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.TECHNOLOGY);
        } else {
            technology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        }
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

        String givenQuantityValue = (String) givenQuantity.getFieldValue();
        String givenQuantityUnitValue = (String) givenQuantityUnit.getFieldValue();
        if (!variousQuantitiesInProductsBySize.isChecked() && (StringUtils.isEmpty(givenQuantityValue)
                || StringUtils.isEmpty(givenQuantityUnitValue))) {
            productBySizeGroupsGrid.setEnabled(false);
        }
    }

}
