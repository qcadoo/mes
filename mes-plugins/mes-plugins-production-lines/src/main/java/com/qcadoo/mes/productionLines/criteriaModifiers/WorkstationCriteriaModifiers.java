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
package com.qcadoo.mes.productionLines.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class WorkstationCriteriaModifiers {

    public void showWorkstationsWithoutProductionLineOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(WorkstationFieldsPL.PRODUCTION_LINE));
    }

    public void showWorkstationsForCurrentProductionLine(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has("productionLine")) {
            Long productionLineId = filterValue.getLong("productionLine");
            scb.createAlias(WorkstationFieldsPL.PRODUCTION_LINE, WorkstationFieldsPL.PRODUCTION_LINE, JoinType.INNER).add(
                    SearchRestrictions.eq(WorkstationFieldsPL.PRODUCTION_LINE + ".id", productionLineId));
        }
    }

    public void showWorkstationsForCurrentDivision(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has("division")) {
            Long divisionId = filterValue.getLong("division");
            scb.createAlias(WorkstationFields.DIVISION, WorkstationFields.DIVISION, JoinType.INNER).add(
                    SearchRestrictions.eq(WorkstationFields.DIVISION + ".id", divisionId));
        }
    }
}
