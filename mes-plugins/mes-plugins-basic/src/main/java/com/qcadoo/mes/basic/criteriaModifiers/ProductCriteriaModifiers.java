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
package com.qcadoo.mes.basic.criteriaModifiers;

import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;
import static com.qcadoo.mes.basic.constants.ProductFields.PARENT;

@Service
public class ProductCriteriaModifiers {

    public static final String L_ASSORTMENT_ID = "assortmentId";

    public static final String L_MODEL_ID = "modelId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showProductFamilyOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(ENTITY_TYPE, ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));
    }

    public void showParticularProductOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue()));
    }

    public void showParticularProductWithoutFamilies(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(PARENT)).add(
                SearchRestrictions.eq(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue()));
    }

    public void showProductsWithoutGivenProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(SubstituteComponentFields.PRODUCT)) {
            Long productId = filterValueHolder.getLong(SubstituteComponentFields.PRODUCT);
            scb.add(SearchRestrictions.idNe(productId));
        }
    }

    public void showProductsWithoutAssortment(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(ProductFields.ASSORTMENT));
    }

    public void showProductsWithModelAndAssortment(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        Long modelId = filterValueHolder.getLong(L_MODEL_ID);

        Entity model = getModelDD().get(modelId);

        List<Entity> products = model.getHasManyField(ModelFields.PRODUCTS);

        for (Entity product : products) {
            scb.add(SearchRestrictions.idNe(product.getId()));
        }

        if (filterValueHolder.has(L_ASSORTMENT_ID)) {
            Long assortmentId = filterValueHolder.getLong(L_ASSORTMENT_ID);

            scb.createAlias(ModelFields.ASSORTMENT, ModelFields.ASSORTMENT, JoinType.LEFT);
            scb.add(SearchRestrictions.or(
                    SearchRestrictions.isNull(ProductFields.ASSORTMENT),
                    SearchRestrictions.eq(ModelFields.ASSORTMENT + ".id", assortmentId)
            ));
        } else {
            scb.add(SearchRestrictions.isNull(ProductFields.ASSORTMENT));
        }
    }

    public void showFamiliesByMachinePartType(final SearchCriteriaBuilder searchCriteriaBuilder,
                                              final FilterValueHolder filterValueHolder) {
        showProductFamilyOnly(searchCriteriaBuilder);
        if (!filterValueHolder.has(ProductFields.MACHINE_PART)) {
            throw new IllegalArgumentException(ProductFields.MACHINE_PART);
        }

        boolean machinePart = filterValueHolder.getBoolean(ProductFields.MACHINE_PART);

        if (machinePart) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(ProductFields.MACHINE_PART, true));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.or(SearchRestrictions.eq(ProductFields.MACHINE_PART, false),
                    SearchRestrictions.isNull(ProductFields.MACHINE_PART)));
        }
    }

    public void showWithGlobalTypeOfMaterialPackage(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(ProductFields.GLOBAL_TYPE_OF_MATERIAL, GlobalTypeOfMaterial.PACKAGE.getStringValue()));
    }

    private DataDefinition getModelDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_MODEL);
    }

}
