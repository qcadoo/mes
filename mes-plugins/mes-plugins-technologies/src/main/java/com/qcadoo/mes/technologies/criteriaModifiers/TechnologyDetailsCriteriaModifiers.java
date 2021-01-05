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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TechnologyDetailsCriteriaModifiers {

    public static final String L_TECHNOLOGY_ID = "technologyId";

    public static final String L_TECHNOLOGY_OPERATION_COMPONENT_ID = "technologyOperationComponentId";

    private static final String L_THIS_ID = "this.id";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showTOCAssignedToTechnology(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(L_TECHNOLOGY_ID)) {
            scb.add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY,
                    TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY,
                    filterValue.getLong(L_TECHNOLOGY_ID)));
        } else {
            scb.add(SearchRestrictions.idEq(-1));
        }
    }

    public void showTechnologicalProcessComponentsAssignedToTOC(final SearchCriteriaBuilder scb,
            final FilterValueHolder filterValue) {
        if (filterValue.has(L_TECHNOLOGY_OPERATION_COMPONENT_ID)) {
            long technologyOperationComponentId = filterValue.getLong(L_TECHNOLOGY_OPERATION_COMPONENT_ID);
            Entity technologyOperationComponent = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                    .get(technologyOperationComponentId);
            if (technologyOperationComponent != null) {
                Entity technologicalProcessList = technologyOperationComponent
                        .getBelongsToField(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_LIST);
                if (technologicalProcessList != null) {
                    SearchCriteriaBuilder subCriteria = dataDefinitionService
                            .get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                                    TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_COMPONENT)
                            .findWithAlias(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_COMPONENT)
                            .add(SearchRestrictions.eqField(
                                    TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_COMPONENT + L_DOT + L_ID, L_THIS_ID))
                            .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_LIST,
                                    technologicalProcessList))
                            .setProjection(SearchProjections.id());
                    scb.add(SearchSubqueries.exists(subCriteria));
                } else {
                    scb.add(SearchRestrictions.idEq(-1));
                }
            } else {
                scb.add(SearchRestrictions.idEq(-1));
            }
        } else {
            scb.add(SearchRestrictions.idEq(-1));
        }
    }

}
