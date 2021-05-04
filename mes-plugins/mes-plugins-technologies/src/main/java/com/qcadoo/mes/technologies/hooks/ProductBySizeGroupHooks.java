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

import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;

@Service
public class ProductBySizeGroupHooks {

    @Autowired
    private UnitConversionService unitConversionService;

    public boolean validatesWith(final DataDefinition productBySizeGroupDD, final Entity productBySizeGroup) {
        boolean isValid = true;

        isValid = isValid && checkIfProductBySizeGroupIsUnique(productBySizeGroupDD, productBySizeGroup);
        isValid = isValid && checkIfUnitOrConverterIsCompatible(productBySizeGroupDD, productBySizeGroup);
        return isValid;

    }

    private boolean checkIfUnitOrConverterIsCompatible(final DataDefinition productBySizeGroupDD, final Entity productBySizeGroup) {
        Entity opic = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT);

        if(!opic.getBooleanField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE)) {

            String opicUnit = opic.getStringField(OperationProductInComponentFields.GIVEN_UNIT);
            String productBySizeGroupUnit = productBySizeGroup.getStringField(ProductBySizeGroupFields.GIVEN_UNIT);

            if(opicUnit.equals(productBySizeGroupUnit)) {
                return true;
            }

            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(productBySizeGroupUnit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT))));
            if (unitConversions.isDefinedFor(opicUnit)) {
                return true;
            } else {
                productBySizeGroup.addGlobalError("technologies.productBySizeGroup.error.wrongUnitOrConversion");
                return false;
            }

        }
        return true;
    }

    private boolean checkIfProductBySizeGroupIsUnique(final DataDefinition productBySizeGroupDD, final Entity productBySizeGroup) {
        Entity operationProductInComponent = productBySizeGroup
                .getBelongsToField(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT);
        Entity sizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.SIZE_GROUP);
        Entity product = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);

        Long productBySizeGroupId = productBySizeGroup.getId();

        SearchCriteriaBuilder searchCriteriaBuilder = productBySizeGroupDD.find();

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT,
                operationProductInComponent));
        searchCriteriaBuilder.add(SearchRestrictions.or(
                SearchRestrictions.belongsTo(ProductBySizeGroupFields.SIZE_GROUP, sizeGroup),
                SearchRestrictions.belongsTo(ProductBySizeGroupFields.PRODUCT, product)));

        if (Objects.nonNull(productBySizeGroupId)) {
            searchCriteriaBuilder.add(SearchRestrictions.idNe(productBySizeGroupId));
        }

        searchCriteriaBuilder.setProjection(alias(id(), "id"));

        List<Entity> productBySizeGroups = searchCriteriaBuilder.list().getEntities();

        if (!productBySizeGroups.isEmpty()) {
            productBySizeGroup.addGlobalError("technologies.productBySizeGroup.error.notUnique");

            return false;
        }

        return true;
    }

}
