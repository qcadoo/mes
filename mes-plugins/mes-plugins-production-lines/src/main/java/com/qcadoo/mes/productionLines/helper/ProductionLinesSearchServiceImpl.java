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
package com.qcadoo.mes.productionLines.helper;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.productionLines.ProductionLinesSearchService;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;

@Service
public class ProductionLinesSearchServiceImpl implements ProductionLinesSearchService {

    // TODO DEV_TEAM - remove this class if unused or add methods supporting new changes to divisions

    private static final String ID_ALIAS = "id";

    private static final String DOT_ID = ".id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Set<Long> findAllLines() {
        return findByTechOrTechGroup(LineSearchMode.ALL, null);
    }

    @Override
    public Set<Long> findLinesSupportingTechnology(final Long technologyId) {
        return findByTechOrTechGroup(LineSearchMode.ALL, technologyId);
    }

    @Override
    public Set<Long> findLinesSupportingTechnologyGroup(final Long technologyGroupId) {
        return findByTechOrTechGroup(LineSearchMode.ALL, technologyGroupId);
    }

    private Set<Long> findByTechOrTechGroup(final LineSearchMode searchMode, final Long techOrTechGroupId) {
        return extractIds(findProjections(searchMode, techOrTechGroupId));
    }

    private List<Entity> findProjections(final LineSearchMode searchMode, final Long techOrTechGroupId) {
        SearchCriteriaBuilder scb = getProductionLineDataDef().find();
        scb.setProjection(alias(id(), "id"));
        searchMode.appendCriteria(scb, techOrTechGroupId);
        return scb.list().getEntities();
    }

    private Set<Long> extractIds(final Iterable<Entity> projections) {
        Set<Long> productionLineIds = Sets.newHashSet();
        for (Entity projection : projections) {
            productionLineIds.add((Long) projection.getField(ID_ALIAS));
        }
        return productionLineIds;
    }

    private DataDefinition getProductionLineDataDef() {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE);
    }

    private enum LineSearchMode {

        ALL {

            @Override
            public void appendCriteria(final SearchCriteriaBuilder scb, final Long id) {
                // DO NOTHING
            }
        };

        protected SearchCriterion matchesId(final String fieldName, final Long id) {
            return eq(fieldName + DOT_ID, id);
        }

        public abstract void appendCriteria(final SearchCriteriaBuilder scb, final Long id);

    }
}
