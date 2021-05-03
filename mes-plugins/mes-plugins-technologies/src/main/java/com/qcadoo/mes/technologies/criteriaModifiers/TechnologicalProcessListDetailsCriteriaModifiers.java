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
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TechnologicalProcessListDetailsCriteriaModifiers {

    public static final String L_TECHNOLOGICAL_PROCESS_LIST_ID = "technologicalProcessListId";

    private static final String L_THIS_ID = "this.id";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showNotAssignedTechnologicalProcesses(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(L_TECHNOLOGICAL_PROCESS_LIST_ID)) {
            long technologicalProcessListId = filterValue.getLong(L_TECHNOLOGICAL_PROCESS_LIST_ID);

            SearchCriteriaBuilder subCriteria = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_COMPONENT)
                    .findWithAlias(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_COMPONENT)
                    .createAlias(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS,
                            TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS, JoinType.INNER)
                    .add(SearchRestrictions.eqField(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS + L_DOT + L_ID, L_THIS_ID))
                    .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_LIST,
                            TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_LIST,
                            technologicalProcessListId))
                    .setProjection(SearchProjections.id());
            scb.add(SearchSubqueries.notExists(subCriteria));
        }
    }

    public void showAssignedTechnologies(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(L_TECHNOLOGICAL_PROCESS_LIST_ID)) {
            long technologicalProcessListId = filterValue.getLong(L_TECHNOLOGICAL_PROCESS_LIST_ID);

            SearchCriteriaBuilder subCriteria = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                    .findWithAlias(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                    .createAlias(TechnologiesConstants.MODEL_TECHNOLOGY, TechnologiesConstants.MODEL_TECHNOLOGY, JoinType.INNER)
                    .add(SearchRestrictions.eqField(TechnologiesConstants.MODEL_TECHNOLOGY + L_DOT + L_ID, L_THIS_ID))
                    .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_LIST,
                            TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_LIST,
                            technologicalProcessListId))
                    .setProjection(SearchProjections.id());

            scb.add(SearchSubqueries.exists(subCriteria));
        } else {
            scb.add(SearchRestrictions.idEq(-1));
        }
    }

    public void filterTechnologies(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(L_TECHNOLOGICAL_PROCESS_LIST_ID)) {
            Long technologicalProcessListId = filterValue.getLong(L_TECHNOLOGICAL_PROCESS_LIST_ID);

            scb.add(SearchRestrictions.eq(TechnologyOperationComponentFields.TECHNOLOGICAL_PROCESS_LIST + L_DOT + L_ID,
                    technologicalProcessListId));
        } else {
            scb.add(SearchRestrictions.idEq(-1));
        }
    }

}
