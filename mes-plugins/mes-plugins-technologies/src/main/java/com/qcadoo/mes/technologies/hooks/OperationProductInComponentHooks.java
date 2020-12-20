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

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.mes.technologies.validators.TechnologyTreeValidators;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OperationProductInComponentHooks {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidators technologyTreeValidators;

    @Autowired
    private TranslationService translationService;

    public boolean validatesWith(final DataDefinition operationProductInComponentDD, final Entity operationProductInComponent) {
        boolean isValid = true;

        isValid = isValid && checkIfTechnologyInputProductTypeOrProductIsSelected(operationProductInComponentDD,
                operationProductInComponent);
        isValid = isValid
                && checkIfOperationProductInComponentIsUnique(operationProductInComponentDD, operationProductInComponent);
        isValid = isValid && technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(operationProductInComponentDD,
                operationProductInComponent);
        isValid = isValid
                && technologyTreeValidators.invalidateIfWrongFormula(operationProductInComponentDD, operationProductInComponent);
        isValid = isValid && technologyService.invalidateIfAlreadyInTheSameOperation(operationProductInComponentDD,
                operationProductInComponent);

        return isValid;
    }

    private boolean checkIfOperationProductInComponentIsUnique(final DataDefinition operationProductInComponentDD,
            final Entity operationProductInComponent) {
        Entity operationComponent = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);
        Entity technologyInputProductType = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
        Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

        Long operationProductInComponentId = operationProductInComponent.getId();

        SearchCriteriaBuilder searchCriteriaBuilder = operationProductInComponentDD.find();

        searchCriteriaBuilder
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.OPERATION_COMPONENT, operationComponent));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE,
                technologyInputProductType));
        searchCriteriaBuilder
                .add(SearchRestrictions.or(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, product),
                        SearchRestrictions.belongsTo(ProductBySizeGroupFields.PRODUCT, product)));

        if (Objects.nonNull(operationProductInComponentId)) {
            searchCriteriaBuilder.add(SearchRestrictions.idNe(operationProductInComponentId));
        }

        searchCriteriaBuilder.setProjection(alias(id(), "id"));

        List<Entity> operationProductInComponents = searchCriteriaBuilder.list().getEntities();

        if (operationProductInComponents.size() > 0) {
            operationProductInComponent.addGlobalError("technologies.operationProductInComponent.error.notUnique");

            return false;
        }

        return true;
    }

    private boolean checkIfTechnologyInputProductTypeOrProductIsSelected(final DataDefinition operationProductInComponentDD,
            final Entity operationProductInComponent) {
        boolean differentProductsInDifferentSizes = operationProductInComponent
                .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);
        Entity technologyInputProductType = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
        Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

        if ((differentProductsInDifferentSizes && Objects.isNull(technologyInputProductType))
                || (Objects.isNull(technologyInputProductType) && Objects.isNull(product))) {
            operationProductInComponent.addGlobalError("technologies.operationProductInComponent.error.requiredFieldNotSelected");

            return false;
        }

        return true;
    }

}
