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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductBySizeGroupHooks {

    public boolean validatesWith(final DataDefinition productBySizeGroupDD, final Entity productBySizeGroup) {
        return checkIfProductBySizeGroupIsUnique(productBySizeGroupDD, productBySizeGroup);
    }

    private boolean checkIfProductBySizeGroupIsUnique(final DataDefinition productBySizeGroupDD,
            final Entity productBySizeGroup) {
        Entity operationProductInComponent = productBySizeGroup
                .getBelongsToField(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT);
        Entity sizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.SIZE_GROUP);
        Entity product = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);

        Long productBySizeGroupId = productBySizeGroup.getId();

        SearchCriteriaBuilder searchCriteriaBuilder = productBySizeGroupDD.find();

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT,
                operationProductInComponent));
        searchCriteriaBuilder
                .add(SearchRestrictions.or(SearchRestrictions.belongsTo(ProductBySizeGroupFields.SIZE_GROUP, sizeGroup),
                        SearchRestrictions.belongsTo(ProductBySizeGroupFields.PRODUCT, product)));

        if (Objects.nonNull(productBySizeGroupId)) {
            searchCriteriaBuilder.add(SearchRestrictions.idNe(productBySizeGroupId));
        }

        searchCriteriaBuilder.setProjection(alias(id(), "id"));

        List<Entity> productBySizeGroups = searchCriteriaBuilder.list().getEntities();

        if (productBySizeGroups.size() > 0) {
            productBySizeGroup.addGlobalError("technologies.productBySizeGroup.error.notUnique");

            return false;
        }

        return true;
    }

}
