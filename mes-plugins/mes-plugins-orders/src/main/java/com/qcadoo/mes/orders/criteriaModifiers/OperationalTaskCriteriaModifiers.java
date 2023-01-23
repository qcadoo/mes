/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orders.criteriaModifiers;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationalTaskCriteriaModifiers {

    public static final String L_WORKSTATION_ID = "workstationId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void filter(final SearchCriteriaBuilder scb, final FilterValueHolder filter) {
        if (filter.has(L_WORKSTATION_ID)) {
            Long workstationId = filter.getLong(L_WORKSTATION_ID);

            scb.createAlias(OperationalTaskFields.WORKSTATION, OperationalTaskFields.WORKSTATION, JoinType.LEFT);

            scb.add(SearchRestrictions.eq(OperationalTaskFields.WORKSTATION + ".id", workstationId));
            scb.add(SearchRestrictions.not(
                    SearchRestrictions.in(OperationalTaskFields.STATE, Lists.newArrayList(OperationalTaskStateStringValues.FINISHED, OperationalTaskStateStringValues.REJECTED))
            ));
        } else {
            scb.add(SearchRestrictions.idEq(-1L));
        }
    }

}