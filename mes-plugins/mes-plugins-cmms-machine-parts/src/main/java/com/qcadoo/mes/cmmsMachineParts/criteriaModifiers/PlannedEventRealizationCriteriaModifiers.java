/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventRealizationFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PlannedEventRealizationCriteriaModifiers {

    public static final String L_PLANNED_EVENT = "plannedEvent";

    public void showActionsForEvent(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_PLANNED_EVENT)) {
            Long eventId = filterValueHolder.getLong(L_PLANNED_EVENT);

            searchCriteriaBuilder.add(SearchRestrictions.eq(PlannedEventRealizationFields.PLANNED_EVENT + ".id", eventId));
        }
    }

}
