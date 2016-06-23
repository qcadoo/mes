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
package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.criteriaModifiers.ProductCriteriaModifiers;
import com.qcadoo.mes.cmmsMachineParts.constants.ProductFieldsCMP;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductCriteriaModifiersCMP {

    public static final String L_MACHINE_PART = "machinePart";

    @Autowired
    private ProductCriteriaModifiers productCriteriaModifiers;

    public void showMachineParts(final SearchCriteriaBuilder searchCriteriaBuilder) {
        searchCriteriaBuilder.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));
    }

    public void showMachinePartsWithoutGivenProduct(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder) {
        productCriteriaModifiers.showProductsWithoutGivenProduct(searchCriteriaBuilder, filterValueHolder);

        searchCriteriaBuilder.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));
    }

    public void showFamiliesByMachinePartType(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder) {
        if (!filterValueHolder.has(L_MACHINE_PART)) {
            throw new IllegalArgumentException(L_MACHINE_PART);
        }

        boolean machinePart = filterValueHolder.getBoolean(L_MACHINE_PART);

        searchCriteriaBuilder.add(SearchRestrictions.eq(ProductFields.ENTITY_TYPE,
                ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));

        if (machinePart) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));

        } else {
            searchCriteriaBuilder.add(SearchRestrictions.or(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, false),
                    SearchRestrictions.isNull(ProductFieldsCMP.MACHINE_PART)));
        }
    }

}
