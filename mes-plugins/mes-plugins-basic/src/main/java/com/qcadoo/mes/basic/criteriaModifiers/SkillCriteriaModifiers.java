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
package com.qcadoo.mes.basic.criteriaModifiers;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
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
public class SkillCriteriaModifiers {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_THIS_ID = "this.id";

    private static final String L_STAFF_ID = "staffId";

    private static final String L_SKILL_ID = "skillId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void filterByStaff(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_STAFF_ID)) {
            SearchCriteriaBuilder subCriteria = getStaffDD().findWithAlias(BasicConstants.MODEL_STAFF)
                    .add(SearchRestrictions.idEq(filterValueHolder.getLong(L_STAFF_ID)))
                    .createAlias(StaffFields.STAFF_SKILLS, StaffFields.STAFF_SKILLS, JoinType.INNER)
                    .createAlias(StaffFields.STAFF_SKILLS + L_DOT + StaffSkillsFields.SKILL, StaffSkillsFields.SKILL,
                            JoinType.INNER)
                    .add(SearchRestrictions.eqField(StaffSkillsFields.SKILL + L_DOT + L_ID, L_THIS_ID))
                    .setProjection(SearchProjections.id());

            scb.add(SearchSubqueries.notExists(subCriteria));
        }
    }

    public void filterBySkill(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_SKILL_ID)) {
            SearchCriteriaBuilder subCriteria = getSkillDD().findWithAlias(BasicConstants.MODEL_SKILL)
                    .add(SearchRestrictions.idEq(filterValueHolder.getLong(L_SKILL_ID)))
                    .createAlias(StaffFields.STAFF_SKILLS, StaffFields.STAFF_SKILLS, JoinType.INNER)
                    .createAlias(StaffFields.STAFF_SKILLS + L_DOT + StaffSkillsFields.STAFF, StaffSkillsFields.STAFF,
                            JoinType.INNER)
                    .add(SearchRestrictions.eqField(StaffSkillsFields.STAFF + L_DOT + L_ID, L_THIS_ID))
                    .setProjection(SearchProjections.id());

            scb.add(SearchSubqueries.notExists(subCriteria));
        }
    }

    private DataDefinition getStaffDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF);
    }

    private DataDefinition getSkillDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SKILL);
    }

}
