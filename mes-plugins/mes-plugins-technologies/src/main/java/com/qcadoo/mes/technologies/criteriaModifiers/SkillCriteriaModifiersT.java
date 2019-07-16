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

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkillCriteriaModifiersT {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_THIS_ID = "this.id";

    private static final String L_OPERATION_ID = "operationId";

    private static final String L_SKILL_ID = "skillId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void filterByOperation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_OPERATION_ID)) {
            SearchCriteriaBuilder subCriteria = getOperationDD().findWithAlias(TechnologiesConstants.MODEL_OPERATION)
                    .add(SearchRestrictions.idEq(filterValueHolder.getLong(L_OPERATION_ID)))
                    .createAlias(OperationFields.OPERATION_SKILLS, OperationFields.OPERATION_SKILLS, JoinType.INNER)
                    .createAlias(OperationFields.OPERATION_SKILLS + L_DOT + OperationSkillFields.SKILL,
                            OperationSkillFields.SKILL, JoinType.INNER)
                    .add(SearchRestrictions.eqField(OperationSkillFields.SKILL + L_DOT + L_ID, L_THIS_ID))
                    .setProjection(SearchProjections.id());

            scb.add(SearchSubqueries.notExists(subCriteria));
        }
    }

    public void filterBySkill(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_SKILL_ID)) {
            SearchCriteriaBuilder subCriteria = getSkillDD().findWithAlias(BasicConstants.MODEL_SKILL)
                    .add(SearchRestrictions.idEq(filterValueHolder.getLong(L_SKILL_ID)))
                    .createAlias(OperationFields.OPERATION_SKILLS, OperationFields.OPERATION_SKILLS, JoinType.INNER)
                    .createAlias(OperationFields.OPERATION_SKILLS + L_DOT + OperationSkillFields.OPERATION,
                            OperationSkillFields.OPERATION, JoinType.INNER)
                    .add(SearchRestrictions.eqField(OperationSkillFields.OPERATION + L_DOT + L_ID, L_THIS_ID))
                    .setProjection(SearchProjections.id());

            scb.add(SearchSubqueries.notExists(subCriteria));
        }
    }

    private DataDefinition getOperationDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION);
    }

    private DataDefinition getSkillDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SKILL);
    }
}
