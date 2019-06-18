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
package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.criteriaModifiers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class DivisionCriteriaModifiersPFTD {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_TECHNOLOGY = "technology";

    public void showDivisionsUsedInTechnology(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {

        if (filterValueHolder.has(L_TECHNOLOGY)) {
            scb.add(SearchRestrictions.in("id", getDivisionsForOrder(filterValueHolder.getLong(L_TECHNOLOGY))));
        } else {
            scb.add(SearchRestrictions.idEq(-1));
        }
    }

    private List<Long> getDivisionsForOrder(final Long technologyId) {
        StringBuilder hql = new StringBuilder();
        hql.append("select division from #basic_division as division ");
        hql.append("join division.technologyOperationComponents toc ");
        hql.append("where toc.technology = :technologyId ");

        SearchQueryBuilder sqb = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_DIVISION).find(
                hql.toString());
        sqb.setLong("technologyId", technologyId);

        List<Entity> divisions = sqb.list().getEntities();

        return divisions.stream().map(division -> division.getId()).collect(Collectors.toList());
    }
}
