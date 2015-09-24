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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PlannedEventRealizationCriteriaModifiers {

    public static final String L_EVENT = "plannedEvent";

    @Autowired
    DataDefinitionService dataDefinitionService;

    public void showActionsForEvent(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_EVENT)) {
            Long eventId = filterValueHolder.getLong(L_EVENT);
            Entity event = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, L_EVENT).get(eventId);
            List<Entity> actions = event.getManyToManyField(PlannedEventFields.ACTIONS);
            List<Long> actionsIds = actions.stream()
                    .map(action -> action.getBelongsToField(ActionForPlannedEventFields.ACTION).getId())
                    .collect(Collectors.toList());
            if (actionsIds.isEmpty()) {
                scb.add(SearchRestrictions.idEq(-1));
            } else {
                scb.add(SearchRestrictions.in("id", actionsIds));
            }
        }
    }
}
