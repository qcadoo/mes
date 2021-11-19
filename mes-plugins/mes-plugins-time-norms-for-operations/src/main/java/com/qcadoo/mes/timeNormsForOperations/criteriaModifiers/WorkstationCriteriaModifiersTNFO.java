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
package com.qcadoo.mes.timeNormsForOperations.criteriaModifiers;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.OperationFieldsTFNO;
import com.qcadoo.mes.timeNormsForOperations.constants.OperationWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class WorkstationCriteriaModifiersTNFO {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_THIS_ID = "this.id";

    public static final String L_OPERATION_ID = "operationId";

    public static final String TECHNOLOGY_OPERATION_COMPONENT_ID = "technologyOperationComponentId";

    public static final String L_WORKSTATION_ID = "workstationId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void filterByOperation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        Long operationId = filterValueHolder.getLong(L_OPERATION_ID);
        EntityList workstations = getOperationDD().get(operationId).getHasManyField(OperationFields.WORKSTATIONS);
        if (!workstations.isEmpty()) {
            Set<Long> workstationIds = workstations.stream().map(Entity::getId).collect(Collectors.toSet());
            scb.add(SearchRestrictions.in("id", workstationIds));
            SearchCriteriaBuilder subCriteria = getOperationDD().findWithAlias(TechnologiesConstants.MODEL_OPERATION)
                    .add(SearchRestrictions.idEq(operationId))
                    .createAlias(OperationFieldsTFNO.OPERATION_WORKSTATION_TIMES, OperationFieldsTFNO.OPERATION_WORKSTATION_TIMES,
                            JoinType.INNER)
                    .createAlias(
                            OperationFieldsTFNO.OPERATION_WORKSTATION_TIMES + L_DOT + OperationWorkstationTimeFields.WORKSTATION,
                            OperationWorkstationTimeFields.WORKSTATION, JoinType.INNER)
                    .add(SearchRestrictions.eqField(OperationWorkstationTimeFields.WORKSTATION + L_DOT + L_ID, L_THIS_ID))
                    .setProjection(SearchProjections.id());

            if (filterValueHolder.has(L_WORKSTATION_ID)) {
                scb.add(SearchRestrictions.or(SearchSubqueries.notExists(subCriteria),
                        SearchRestrictions.idEq(filterValueHolder.getLong(L_WORKSTATION_ID))));
            } else {
                scb.add(SearchSubqueries.notExists(subCriteria));
            }
        } else {
            scb.add(SearchRestrictions.idEq(-1));
        }
    }

    public void filterByTOC(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        Long technologyOperationComponentId = filterValueHolder.getLong(TECHNOLOGY_OPERATION_COMPONENT_ID);
        EntityList workstations = getTOCDD().get(technologyOperationComponentId)
                .getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS);
        if (!workstations.isEmpty()) {
            Set<Long> workstationIds = workstations.stream().map(Entity::getId).collect(Collectors.toSet());
            scb.add(SearchRestrictions.in("id", workstationIds));
            SearchCriteriaBuilder subCriteria = getTOCDD()
                    .findWithAlias(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                    .add(SearchRestrictions.idEq(technologyOperationComponentId))
                    .createAlias(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_WORKSTATION_TIMES,
                            TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_WORKSTATION_TIMES, JoinType.INNER)
                    .createAlias(
                            TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_WORKSTATION_TIMES + L_DOT
                                    + TechOperCompWorkstationTimeFields.WORKSTATION,
                            TechOperCompWorkstationTimeFields.WORKSTATION, JoinType.INNER)
                    .add(SearchRestrictions.eqField(TechOperCompWorkstationTimeFields.WORKSTATION + L_DOT + L_ID, L_THIS_ID))
                    .setProjection(SearchProjections.id());

            if (filterValueHolder.has(L_WORKSTATION_ID)) {
                scb.add(SearchRestrictions.or(SearchSubqueries.notExists(subCriteria),
                        SearchRestrictions.idEq(filterValueHolder.getLong(L_WORKSTATION_ID))));
            } else {
                scb.add(SearchSubqueries.notExists(subCriteria));
            }
        } else {
            scb.add(SearchRestrictions.idEq(-1));
        }
    }

    private DataDefinition getOperationDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION);
    }

    private DataDefinition getTOCDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }
}
