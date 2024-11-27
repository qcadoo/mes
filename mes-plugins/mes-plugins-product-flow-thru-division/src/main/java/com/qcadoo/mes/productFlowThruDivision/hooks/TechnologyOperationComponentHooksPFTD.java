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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.materialFlowResources.constants.DivisionFieldsMFR;
import com.qcadoo.mes.productFlowThruDivision.constants.DivisionFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TechnologyOperationComponentHooksPFTD {

    public void onSave(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        fillRangeAndDivision(technologyOperationComponentDD, technologyOperationComponent);
    }

    private void fillRangeAndDivision(DataDefinition technologyOperationComponentDD,
                                      Entity technologyOperationComponent) {
        if (technologyOperationComponent.isCopied()) {
            return;
        }
        Entity division = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.DIVISION);
        if (division != null) {
            Long technologyOperationComponentId = technologyOperationComponent.getId();
            Entity technologyOperationComponentDB = null;
            if (technologyOperationComponentId != null) {
                technologyOperationComponentDB = technologyOperationComponentDD.get(technologyOperationComponentId);
            }
            if (technologyOperationComponentId == null || technologyOperationComponentDB.getBelongsToField(TechnologyOperationComponentFields.DIVISION) == null
                    || !division.equals(technologyOperationComponentDB.getBelongsToField(TechnologyOperationComponentFields.DIVISION))) {
                Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
                technology = technology.getDataDefinition().get(technology.getId());
                List<Entity> tocs = getTechnologyOperationComponents(technologyOperationComponentDD, technology);
                Set<Long> divisionIds = tocs.stream()
                        .filter(e -> technologyOperationComponentId == null || !e.getId().equals(technologyOperationComponentId))
                        .filter(e -> e.getBelongsToField(TechnologyOperationComponentFields.DIVISION) != null)
                        .map(e -> e.getBelongsToField(TechnologyOperationComponentFields.DIVISION).getId()).collect(Collectors.toSet());
                if (divisionIds.size() <= 1 && (divisionIds.size() != 1 || divisionIds.contains(division.getId()))) {
                    fillLocationsForOneDivisionRange(technology, division);
                    technology.getDataDefinition().fastSave(technology);
                }
            }
        }
    }

    private void fillLocationsForOneDivisionRange(Entity technology, Entity division) {
        technology.setField(TechnologyFieldsPFTD.COMPONENTS_LOCATION, division.getBelongsToField(DivisionFieldsMFR.COMPONENTS_LOCATION));
        technology.setField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION, division.getBelongsToField(DivisionFieldsMFR.COMPONENTS_OUTPUT_LOCATION));
        technology.setField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION, division.getBelongsToField(DivisionFieldsMFR.PRODUCTS_INPUT_LOCATION));
        technology.setField(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE, division.getBelongsToField(DivisionFieldsPFTD.WASTE_RECEPTION_WAREHOUSE));
        technology.setField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION, division.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION));
        technology.setField(TechnologyFieldsPFTD.PRODUCTION_FLOW, division.getStringField(TechnologyFieldsPFTD.PRODUCTION_FLOW));
    }

    private List<Entity> getTechnologyOperationComponents(DataDefinition technologyOperationComponentDD,
                                                          final Entity technology) {
        return technologyOperationComponentDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();
    }

}
