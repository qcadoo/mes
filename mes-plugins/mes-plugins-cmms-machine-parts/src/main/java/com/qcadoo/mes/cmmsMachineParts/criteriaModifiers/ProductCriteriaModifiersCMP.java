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

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.criteriaModifiers.ProductCriteriaModifiers;
import com.qcadoo.mes.cmmsMachineParts.constants.ProductFieldsCMP;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;

@Service
public class ProductCriteriaModifiersCMP {

    public static final String MACHINE_PART_FILTER_PARAMETER = "machinePart";

    @Autowired
    private ProductCriteriaModifiers productCriteriaModifiers;

    public void showMachineParts(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));
    }

    public void showMachinePartsWithoutGivenProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filter) {
        productCriteriaModifiers.showProductsWithoutGivenProduct(scb, filter);
        scb.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));
    }

    public void showFamiliesByMachinePartType(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if(!filterValue.has(MACHINE_PART_FILTER_PARAMETER)){
            throw new IllegalArgumentException(MACHINE_PART_FILTER_PARAMETER);
        }

        boolean machinePart = filterValue.getBoolean(MACHINE_PART_FILTER_PARAMETER);

        scb.add(SearchRestrictions.eq(ENTITY_TYPE, ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));
        if(machinePart){
            scb.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));

        } else {
            scb.add(SearchRestrictions.or(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, false), SearchRestrictions.isNull(ProductFieldsCMP.MACHINE_PART)));
        }
    }

}
