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

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaffCriteriaModifiers {

    private static final String DIVISION_ID = "division_id";

    private static final String L_PRODUCTION_LINE_ID = "productionLine_id";

    private static final String L_PRODUCTION_LINES = "productionLines";

    private static final String L_PRODUCTION_LINE = "productionLine";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showStaffWithoutCrew(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(StaffFields.CREW));
    }

    public void selectWorkstation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {

        if (filterValueHolder.has(DIVISION_ID)) {
            Long divisionId = filterValueHolder.getLong(DIVISION_ID);

            scb.createAlias(WorkstationFields.DIVISION, WorkstationFields.DIVISION, JoinType.INNER).add(
                    SearchRestrictions.eq(WorkstationFields.DIVISION + ".id", divisionId));
        }

        if (filterValueHolder.has(L_PRODUCTION_LINE_ID)) {
            Long productionLineId = filterValueHolder.getLong(L_PRODUCTION_LINE_ID);

            scb.createAlias(L_PRODUCTION_LINE, L_PRODUCTION_LINE,
                    JoinType.INNER).add(SearchRestrictions.eq(L_PRODUCTION_LINE + ".id", productionLineId));

        }
    }

    public void selectProductionLine(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(DIVISION_ID)) {
            Long divisionId = filterValueHolder.getLong(DIVISION_ID);

            Entity division = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_DIVISION).get(
                    divisionId);

            List<Long> productionLinesIds = division.getHasManyField(L_PRODUCTION_LINES).stream()
                    .map(Entity::getId).collect(Collectors.toList());

            if (productionLinesIds.isEmpty()) {
                return;
            }

            scb.add(SearchRestrictions.in("id", productionLinesIds));
        }
    }
}
