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
package com.qcadoo.mes.technologies.hooks;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class OperationSkillDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_SKILL_IDS = "skillIds";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent operationSkillForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent skillLookup = (LookupComponent) view.getComponentByReference(OperationSkillFields.SKILL);

        Entity operationSkill = operationSkillForm.getEntity();
        Entity operation = operationSkill.getBelongsToField(OperationSkillFields.OPERATION);

        filterSkillLookup(skillLookup, operation);
    }

    private void filterSkillLookup(final LookupComponent skillLookup, final Entity operation) {
        FilterValueHolder filterValueHolder = skillLookup.getFilterValue();

        List<Long> skillIds = Lists.newArrayList();

        if (!Objects.isNull(operation)) {
            Optional<List<Long>> mayBeSkillIds = getOperationSkillIds(operation);

            if (mayBeSkillIds.isPresent()) {
                skillIds = mayBeSkillIds.get();
            }
        }

        if (skillIds.isEmpty()) {
            filterValueHolder.remove(L_SKILL_IDS);
        } else {
            filterValueHolder.put(L_SKILL_IDS, skillIds);
        }

        skillLookup.setFilterValue(filterValueHolder);
    }

    private Optional<List<Long>> getOperationSkillIds(final Entity operation) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT skill_id FROM technologies_operationskill ");
        query.append("WHERE operation_id = :operationId");

        Map<String, Object> params = Maps.newHashMap();

        params.put("operationId", operation.getId());

        List<Long> skillIds;

        try {
            skillIds = jdbcTemplate.queryForList(query.toString(), params, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

        return Optional.of(skillIds);
    }
}
