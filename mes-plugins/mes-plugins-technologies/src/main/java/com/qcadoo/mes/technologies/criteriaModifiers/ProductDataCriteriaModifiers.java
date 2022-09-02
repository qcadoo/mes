/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.technologies.criteriaModifiers;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductDataCriteriaModifiers {

    public static final String TECHNOLOGY_ID = "technologyId";

    public static final String OPERATION_PRODUCT_IN_COMPONENT_IDS = "operationProductInComponentIds";

    public static final String TECHNOLOGY_OPERATION_COMPONENT_IDS = "technologyOperationComponentIds";

    public void restrictProductData(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(TECHNOLOGY_ID)) {
            Long id = filterValue.getLong(TECHNOLOGY_ID);

            scb.add(SearchRestrictions.eq(TECHNOLOGY_ID, id.intValue()));
        }
        if (filterValue.has(OPERATION_PRODUCT_IN_COMPONENT_IDS)) {
            List<Long> ids = filterValue.getListOfLongs(OPERATION_PRODUCT_IN_COMPONENT_IDS);

            scb.add(SearchRestrictions.not(SearchRestrictions.in("id", ids)));
        }
        if (filterValue.has(TECHNOLOGY_OPERATION_COMPONENT_IDS)) {
            List<Long> ids = filterValue.getListOfLongs(TECHNOLOGY_OPERATION_COMPONENT_IDS);

            scb.add(SearchRestrictions.not(SearchRestrictions.in("id", ids)));
        }
    }

}
