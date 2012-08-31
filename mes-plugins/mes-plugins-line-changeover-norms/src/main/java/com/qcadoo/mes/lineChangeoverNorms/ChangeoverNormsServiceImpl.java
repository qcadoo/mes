/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ChangeoverNormsServiceImpl implements ChangeoverNormsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Override
    public Entity getMatchingChangeoverNorms(final Entity fromTechnology, final Entity toTechnology, final Entity productionLine) {
        Entity matchingNorm = changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology,
                toTechnology, productionLine);
        if (matchingNorm == null) {
            matchingNorm = changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology,
                    toTechnology, null);
        }
        if (matchingNorm == null) {
            Entity fromTechnologyGroup = getTechnologyGroupForTechnology(fromTechnology);
            Entity toTechnologyGroup = getTechnologyGroupForTechnology(toTechnology);
            if (fromTechnologyGroup == null || toTechnologyGroup == null) {
                return matchingNorm;
            }
            matchingNorm = changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(
                    fromTechnologyGroup, toTechnologyGroup, productionLine);
            if (matchingNorm == null) {
                matchingNorm = changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(
                        fromTechnologyGroup, toTechnologyGroup, null);
            }
        }
        return matchingNorm;
    }

    private Entity getTechnologyGroupForTechnology(final Entity technology) {
        Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
        if (technologyGroup != null) {
            return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_GROUP).get(technologyGroup.getId());
        }
        return null;
    }
}
