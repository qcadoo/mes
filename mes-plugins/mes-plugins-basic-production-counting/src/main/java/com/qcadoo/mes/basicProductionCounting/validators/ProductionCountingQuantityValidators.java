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
package com.qcadoo.mes.basicProductionCounting.validators;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.basicProductionCounting.hooks.util.ProductionProgressModifyLockHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class ProductionCountingQuantityValidators {

    @Autowired
    private ProductionProgressModifyLockHelper progressModifyLockHelper;

    public boolean validatesWith(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        boolean isValid = true;

        isValid = isValid && checkRoleAndTypeOfMaterial(productionCountingQuantityDD, productionCountingQuantity);
        isValid = isValid && checkIfIsUnique(productionCountingQuantityDD, productionCountingQuantity);

        return isValid;
    }

    public boolean validatePlannedQuantity(final DataDefinition productionCountingQuantityDD,
            final FieldDefinition plannedQuantityFieldDefinition, final Entity productionCountingQuantity, final Object oldValue,
            final Object newValue) {
        // I don't check if entity is updated or created (check null on id) because we should disallow also creating
        // of new ones if editing production progresses for accepted orders is locked.
        if (ObjectUtils.equals(oldValue, newValue)) {
            return true;
        }
        if (progressModifyLockHelper.isLocked(productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.ORDER))) {
            productionCountingQuantity
                    .addError(plannedQuantityFieldDefinition,
                            "basicProductionCounting.productionCountingQuantity.plannedQuantity.error.valueChangeIsNotAllowedForAcceptetOrder");
            return false;
        }
        return true;
    }

    private boolean checkRoleAndTypeOfMaterial(final DataDefinition productionCountingQuantityDD,
            final Entity productionCountingQuantity) {
        String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);
        String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

        if (isRoleUsed(role)) {
            if (isTypeOfMaterialFinalProduct(typeOfMaterial) || isTypeOfMaterialWaste(typeOfMaterial)) {
                productionCountingQuantity.addError(productionCountingQuantityDD.getField(ProductionCountingQuantityFields.ROLE),
                        "basicProductionCounting.productionCountingQuantity.role.error.finalProductOrWasteHasToBeProduced");

                return false;
            }
        } else if (isRoleProduced(role)) {
            if (isTypeOfMaterialComponent(typeOfMaterial)) {
                productionCountingQuantity.addError(productionCountingQuantityDD.getField(ProductionCountingQuantityFields.ROLE),
                        "basicProductionCounting.productionCountingQuantity.role.error.componentHasToBeUsed");

                return false;
            } else if (isTypeOfMaterialIntermediate(typeOfMaterial)) {
                Entity technologyOperationComponent = productionCountingQuantity
                        .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (technologyOperationComponent == null) {
                    productionCountingQuantity
                            .addError(productionCountingQuantityDD
                                    .getField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                                    "basicProductionCounting.productionCountingQuantity.technologyOperationComponent.error.technologyOperationComponentRequired");

                    return false;
                }
            } else if (isTypeOfMaterialFinalProduct(typeOfMaterial)) {
                if (!checkIfAnotherFinalProductExists(productionCountingQuantityDD, productionCountingQuantity)) {
                    productionCountingQuantity.addError(
                            productionCountingQuantityDD.getField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL),
                            "basicProductionCounting.productionCountingQuantity.typeOfMaterial.error.anotherFinalProductExists");

                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkIfIsUnique(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        if (!checkIfProductionCountingQuantityIsUnique(productionCountingQuantityDD, productionCountingQuantity)) {
            productionCountingQuantity.addGlobalError("basicProductionCounting.productionCountingQuantity.error.isNotUnique");

            return false;
        }

        return true;
    }

    private boolean checkIfProductionCountingQuantityIsUnique(final DataDefinition productionCountingQuantityDD,
            final Entity productionCountingQuantity) {
        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity technologyOperationComponent = productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
        String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);
        String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

        SearchCriteriaBuilder searchCriteriaBuilder = productionCountingQuantityDD
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, role))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL, typeOfMaterial));

        if (productionCountingQuantity.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", productionCountingQuantity.getId()));
        }

        SearchResult searchResult = searchCriteriaBuilder.list();

        return searchResult.getEntities().isEmpty();
    }

    private boolean checkIfAnotherFinalProductExists(final DataDefinition productionCountingQuantityDD,
            final Entity productionCountingQuantity) {
        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);

        SearchCriteriaBuilder searchCriteriaBuilder = productionCountingQuantityDD
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                        ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue()));

        if (productionCountingQuantity.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", productionCountingQuantity.getId()));
        }

        SearchResult searchResult = searchCriteriaBuilder.list();

        return searchResult.getEntities().isEmpty();
    }

    private boolean isRoleUsed(final String role) {
        return ProductionCountingQuantityRole.USED.getStringValue().equals(role);
    }

    private boolean isRoleProduced(final String role) {
        return ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role);
    }

    private boolean isTypeOfMaterialComponent(final String typeOfMaterial) {
        return ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial);
    }

    private boolean isTypeOfMaterialIntermediate(final String typeOfMaterial) {
        return ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(typeOfMaterial);
    }

    private boolean isTypeOfMaterialFinalProduct(final String typeOfMaterial) {
        return ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial);
    }

    private boolean isTypeOfMaterialWaste(final String typeOfMaterial) {
        return ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(typeOfMaterial);
    }

}
