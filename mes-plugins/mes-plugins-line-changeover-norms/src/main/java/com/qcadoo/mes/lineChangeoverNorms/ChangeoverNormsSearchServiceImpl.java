/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.lineChangeoverNorms;

import static com.qcadoo.model.api.search.SearchRestrictions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;

@Service
public class ChangeoverNormsSearchServiceImpl implements ChangeoverNormsSearchService {

    private static final String DOT_ID = ".id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity findBestMatching(final Long fromTechnologyId, final Long fromTechnologyGroupId, final Long toTechnologyId,
            final Long toTechnologyGroupId, final Long productionLineId) {
        SearchCriteriaBuilder scb = getChangeoverDataDef().find();

        scb.add(getTechnologiesRestrictions(fromTechnologyId, fromTechnologyGroupId, toTechnologyId, toTechnologyGroupId));
        scb.add(getProductionLineRestrictions(productionLineId));

        // for specific technologies first
        scb.addOrder(SearchOrders.asc(LineChangeoverNormsFields.CHANGEOVER_TYPE));
        // with production line defined first
        scb.addOrder(SearchOrders.asc(LineChangeoverNormsFields.PRODUCTION_LINE));
        // newest wins
        scb.addOrder(SearchOrders.desc("id"));

        return scb.setMaxResults(1).uniqueResult();
    }

    private SearchCriterion getProductionLineRestrictions(final Long productionLineId) {
        SearchCriterion matchProductionLine = eq(LineChangeoverNormsFields.PRODUCTION_LINE + DOT_ID, productionLineId);
        SearchCriterion productionLineIsNull = isNull(LineChangeoverNormsFields.PRODUCTION_LINE);
        return or(matchProductionLine, productionLineIsNull);
    }

    private SearchCriterion getTechnologiesRestrictions(final Long fromTechnologyId, final Long fromTechnologyGroupId,
            final Long toTechnologyId, final Long toTechnologyGroupId) {
        SearchCriterion matchTechnologies = getPairRestriction(LineChangeoverNormsFields.FROM_TECHNOLOGY, fromTechnologyId,
                LineChangeoverNormsFields.TO_TECHNOLOGY, toTechnologyId);
        SearchCriterion matchTechnologyGroups = getPairRestriction(LineChangeoverNormsFields.TO_TECHNOLOGY,
                fromTechnologyGroupId, LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP, toTechnologyGroupId);

        Preconditions.checkArgument(matchTechnologies != null || matchTechnologyGroups != null,
                "you have to provide pair of technologies or pair of technology groups.");

        SearchDisjunction disjunction = SearchRestrictions.disjunction();
        if (matchTechnologies != null) {
            disjunction.add(matchTechnologies);
        }
        if (matchTechnologyGroups != null) {
            disjunction.add(matchTechnologyGroups);
        }
        return disjunction;
    }

    private SearchCriterion getPairRestriction(final String leftFieldName, final Long leftId, final String rightFieldName,
            final Long rightId) {
        if (leftId == null || rightId == null) {
            return null;
        }
        return and(eq(leftFieldName + DOT_ID, leftId), eq(rightFieldName + DOT_ID, rightId));
    }

    @Override
    @Deprecated
    public Entity searchMatchingChangeroverNormsForTechnologyWithLine(final Entity fromTechnology, final Entity toTechnology,
            final Entity productionLine) {
        return findBestMatching(fromTechnology.getId(), null, toTechnology.getId(), null, productionLine.getId());
    }

    @Override
    @Deprecated
    public Entity searchMatchingChangeroverNormsForTechnologyGroupWithLine(final Entity fromTechnologyGroup,
            final Entity toTechnologyGroup, final Entity productionLine) {
        return findBestMatching(null, fromTechnologyGroup.getId(), null, toTechnologyGroup.getId(), productionLine.getId());
    }

    private DataDefinition getChangeoverDataDef() {
        return dataDefinitionService.get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER,
                LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS);
    }

}
