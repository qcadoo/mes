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
package com.qcadoo.mes.orders.criteriaModifiers;

import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionLineCriteriaModifiersO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public static final String TECHNOLOGY_ID = "technology_id";

    public void filterProductionLines(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {

        scb.add(SearchRestrictions.eq(ProductionLineFields.PRODUCTION, true));
        if(filterValueHolder.has(TECHNOLOGY_ID)) {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY)
                    .get(filterValueHolder.getLong(TECHNOLOGY_ID));

            List<Entity> lines = technology.getHasManyField(TechnologyFields.PRODUCTION_LINES);
            if(!lines.isEmpty()) {
                List<Long> productionLinesIds = lines.stream()
                        .map(pl -> pl.getBelongsToField(TechnologyProductionLineFields.PRODUCTION_LINE).getId()).collect(Collectors.toList());
                scb.add(SearchRestrictions.in("id", productionLinesIds));
            }
        }

    }

}
