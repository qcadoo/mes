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
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.constants.UnitConversionItemFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductHooks {

    @Autowired
    private ProductService productService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCreate(final DataDefinition productDD, final Entity product) {
        productService.fillUnit(productDD, product);
        calculateConversionOnCreate(product);
        fillExpiryDateValidityUnit(product);
    }

    private void calculateConversionOnCreate(final Entity product) {
        productService.conversionForProductUnit(product);
    }

    private void fillExpiryDateValidityUnit(final Entity product) {
        if (Objects.isNull(product.getField(ProductFields.EXPIRY_DATE_VALIDITY_UNIT))) {
            product.setField(ProductFields.EXPIRY_DATE_VALIDITY_UNIT, ExpiryDateValidityUnit.MONTHS.getStringValue());
        }
    }

    public void onSave(final DataDefinition productDD, final Entity product) {
        updateModelAndAssortment(productDD, product);
    }

    private void updateModelAndAssortment(final DataDefinition productDD, final Entity product) {
        Long productId = product.getId();
        Entity assortment = product.getBelongsToField(ProductFields.ASSORTMENT);
        Entity model = product.getBelongsToField(ProductFields.MODEL);

        boolean isAssortmentAndModelNonNull = Objects.nonNull(assortment) && Objects.nonNull(model);

        if (Objects.nonNull(productId)) {
            Entity productFromDB = productDD.get(productId);

            Entity assortmentFromDB = productFromDB.getBelongsToField(ProductFields.ASSORTMENT);

            boolean areSame = (Objects.isNull(assortment) ? Objects.isNull(assortmentFromDB)
                    : (Objects.nonNull(assortmentFromDB) && assortment.getId().equals(assortmentFromDB.getId())));

            if (areSame) {
                if (Objects.nonNull(model) && Objects.isNull(assortment)) {
                    Entity modelAssortment = product.getBelongsToField(ProductFields.MODEL_ASSORTMENT);

                    if (Objects.isNull(modelAssortment)) {
                        modelAssortment = model.getBelongsToField(ModelFields.ASSORTMENT);
                    }

                    product.setField(ProductFields.ASSORTMENT, modelAssortment);
                }
            } else {
                if (isAssortmentAndModelNonNull) {
                    Entity modelAssortment = product.getBelongsToField(ProductFields.MODEL_ASSORTMENT);

                    if (Objects.isNull(modelAssortment)) {
                        modelAssortment = model.getBelongsToField(ModelFields.ASSORTMENT);
                    }

                    if (Objects.nonNull(modelAssortment) && !modelAssortment.getId().equals(assortment.getId())) {
                        product.setField(ProductFields.MODEL, null);
                    }
                }
            }
        } else {
            if (isAssortmentAndModelNonNull) {
                Entity modelAssortment = model.getBelongsToField(ModelFields.ASSORTMENT);

                if (Objects.nonNull(modelAssortment) && !modelAssortment.getId().equals(assortment.getId())) {
                    product.setField(ProductFields.MODEL, null);
                }
            }
        }
    }

    public boolean checkIfParentIsFamily(final DataDefinition productDD, final Entity product) {
        Entity parent = product.getBelongsToField(ProductFields.PARENT);

        if (Objects.isNull(parent)) {
            return true;
        }

        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(parent.getStringField(ProductFields.ENTITY_TYPE))) {
            return true;
        } else {
            product.addError(productDD.getField(ProductFields.PARENT), "basic.product.parent.parentIsNotFamily");

            return false;
        }
    }

    public void onCopy(final DataDefinition productDD, final Entity product) {
        clearExternalIdOnCopy(productDD, product);
        clearFieldsOnCopy(product);
    }

    public void clearExternalIdOnCopy(final DataDefinition productDD, final Entity product) {
        if (Objects.isNull(product)) {
            return;
        }

        product.setField(ProductFields.EXTERNAL_NUMBER, null);
    }

    private void clearFieldsOnCopy(final Entity product) {
        if (Objects.isNull(product)) {
            return;
        }

        product.setField(ProductFields.ADDITIONAL_UNIT, null);
        product.setField(ProductFields.EAN, null);
    }

    public void calculateConversionIfUnitChanged(final DataDefinition productDD, final Entity product) {
        if (productService.hasUnitChangedOnUpdate(product)) {
            productService.conversionForProductUnit(product);
        }
    }

    public boolean validateAdditionalUnit(final DataDefinition productDD, final Entity product) {
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        String defaultUnit = product.getStringField(ProductFields.UNIT);

        if (!StringUtils.isEmpty(additionalUnit)) {
            if (additionalUnit.equals(defaultUnit)) {
                product.addError(productDD.getField(ProductFields.ADDITIONAL_UNIT),
                        "basic.product.additionalUnit.error.sameUnits");

                return false;
            }

            List<Entity> conversions = product.getHasManyField(ProductFields.CONVERSION_ITEMS);

            if (conversions.stream().noneMatch(
                    conversionItem -> conversionItem.getStringField(UnitConversionItemFields.UNIT_TO).equals(additionalUnit)
                            && conversionItem.getStringField(UnitConversionItemFields.UNIT_FROM).equals(defaultUnit))) {
                product.addGlobalError("basic.product.additionalUnit.error.unitConversionMissing");

                return false;
            }
        }

        return true;
    }

    public boolean validateProductAttributeValues(final DataDefinition productDD, final Entity product) {
        String entityType = product.getStringField(ProductFields.ENTITY_TYPE);
        List<Entity> productAttributeValues = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

        if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType) &&
                productAttributeValues.stream().anyMatch(productAttributeValue ->
                        Objects.isNull(productAttributeValue.getStringField(ProductAttributeValueFields.VALUE)))) {
            product.addGlobalError("basic.product.productAttributeValues.error.valuesEmpty");

            return false;
        }

        return true;
    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        LookupComponent parentLookup = (LookupComponent) view.getComponentByReference(ProductFields.PARENT);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity product = form.getPersistedEntityWithIncludedFormValues();

        FilterValueHolder holder = parentLookup.getFilterValue();
        holder.put(ProductFields.MACHINE_PART, product.getBooleanField(ProductFields.MACHINE_PART));
        parentLookup.setFilterValue(holder);
    }

}
