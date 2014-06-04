/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionCounting.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TechnologyOperationComponentCriteriaModifiersPC {

    private static final String L_ID = "id";

    private static final String L_TECHNOLOGY = "technology";

    public void restrictTechnologyOperationComponentsToOrderTechnology(final SearchCriteriaBuilder scb,
            final FilterValueHolder filterValue) {
        if (!filterValue.has(L_TECHNOLOGY)) {
            throw new IllegalArgumentException("Chain parameter is required");
        }

        Long technologyId = filterValue.getLong(L_TECHNOLOGY);
        scb.createAlias(L_TECHNOLOGY, L_TECHNOLOGY).add(SearchRestrictions.eq(L_TECHNOLOGY + "." + L_ID, technologyId));
    }

}
