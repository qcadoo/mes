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
package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import com.qcadoo.mes.materialFlowResources.constants.ResourceDtoFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ResourceCriteriaModifiers {

    private static final String L_PRODUCT = "product";

    public static final String L_LOCATION_FROM = "locationFrom";

    public void showResourcesForProductInWarehouse(final SearchCriteriaBuilder scb,
                                                   final FilterValueHolder filterValue) {

        if (!filterValue.has(L_PRODUCT) || !filterValue.has(L_LOCATION_FROM)) {
            return;
        }

        Long productId = filterValue.getLong(L_PRODUCT);
        Long locationId = filterValue.getLong(L_LOCATION_FROM);
        scb.createAlias(ResourceFields.PRODUCT, L_PRODUCT, JoinType.INNER)
                .createAlias(ResourceFields.LOCATION, L_LOCATION_FROM, JoinType.INNER)
                .add(SearchRestrictions.eq(L_PRODUCT + ".id", productId))
                .add(SearchRestrictions.eq(L_LOCATION_FROM + ".id", locationId));
    }

    public void restrictDtoToLocation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        scb.add(SearchRestrictions.eq(ResourceDtoFields.BLOCKED_FOR_QUALITY_CONTROL, false));
        if (!filterValue.has(L_LOCATION_FROM)) {
            return;
        }

        Long locationId = filterValue.getLong(L_LOCATION_FROM);
        scb.add(SearchRestrictions.eq(ResourceDtoFields.LOCATION_ID, locationId.intValue())).add(
                SearchRestrictions.gt(ResourceDtoFields.AVAILABLE_QUANTITY, BigDecimal.ZERO));
    }

    public void restrictToLocation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        scb.add(SearchRestrictions.eq(ResourceFields.BLOCKED_FOR_QUALITY_CONTROL, false));
        if (!filterValue.has(L_LOCATION_FROM)) {
            return;
        }

        Long locationId = filterValue.getLong(L_LOCATION_FROM);
        scb.createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.INNER)
                .add(SearchRestrictions.eq(ResourceFields.LOCATION + ".id", locationId)).
                add(SearchRestrictions.gt(ResourceDtoFields.AVAILABLE_QUANTITY, BigDecimal.ZERO));
    }

}
