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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PlannedEventsCriteriaModifiers {

    public void filterRealizedAndCancelledEvents(SearchCriteriaBuilder searchCriteriaBuilder, FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(PlannedEventFields.NUMBER)) {
            searchCriteriaBuilder.add(SearchRestrictions.ne(PlannedEventFields.NUMBER,
                    filterValueHolder.getString(PlannedEventFields.NUMBER)));
        }

        searchCriteriaBuilder.add(SearchRestrictions.ne(PlannedEventFields.STATE, PlannedEventStateStringValues.REALIZED)).add(
                SearchRestrictions.ne(PlannedEventFields.STATE, PlannedEventStateStringValues.CANCELED));
    }

}
