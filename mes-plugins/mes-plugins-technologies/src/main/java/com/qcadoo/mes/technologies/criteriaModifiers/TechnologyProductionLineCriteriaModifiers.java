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
package com.qcadoo.mes.technologies.criteriaModifiers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.productionLines.constants.DivisionFieldsPL;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TechnologyProductionLineCriteriaModifiers {

    public static final String L_TECHNOLOGY_ID = "technologyId";

    public static final String L_DIVISION_ID = "divisionId";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_THIS_ID = "this.id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void filterByTechnologyAndDivision(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(ProductionLineFields.PRODUCTION, true));
        if (filterValueHolder.has(L_TECHNOLOGY_ID)) {
            SearchCriteriaBuilder subCriteria = getTechnologyDD().findWithAlias(TechnologiesConstants.MODEL_TECHNOLOGY)
                    .add(SearchRestrictions.idEq(filterValueHolder.getLong(L_TECHNOLOGY_ID)))
                    .createAlias(TechnologyFields.PRODUCTION_LINES, TechnologyFields.PRODUCTION_LINES, JoinType.INNER)
                    .createAlias(TechnologyFields.PRODUCTION_LINES + L_DOT + TechnologyProductionLineFields.PRODUCTION_LINE,
                            TechnologyProductionLineFields.PRODUCTION_LINE, JoinType.INNER)
                    .add(SearchRestrictions.eqField(TechnologyProductionLineFields.PRODUCTION_LINE + L_DOT + L_ID, L_THIS_ID))
                    .setProjection(SearchProjections.id());

            scb.add(SearchSubqueries.notExists(subCriteria));
        }
        if (filterValueHolder.has(L_DIVISION_ID)) {
            Long divisionId = filterValueHolder.getLong(L_DIVISION_ID);
            Entity division = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_DIVISION)
                    .get(divisionId);
            List<Long> productionLinesIds = division.getHasManyField(DivisionFieldsPL.PRODUCTION_LINES).stream().map(Entity::getId).collect(Collectors.toList());

            if (!productionLinesIds.isEmpty()) {
                scb.add(SearchRestrictions
                        .in(L_ID, productionLinesIds));
            }
        }

    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }
}
