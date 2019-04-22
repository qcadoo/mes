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
package com.qcadoo.mes.basic.hooks;

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
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class StaffSkillDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_SKILL_IDS = "skillIds";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent staffSkillForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent skillLookup = (LookupComponent) view.getComponentByReference(StaffSkillsFields.SKILL);

        Entity staffSkill = staffSkillForm.getEntity();
        Entity staff = staffSkill.getBelongsToField(StaffSkillsFields.STAFF);

        filterSkillLookup(skillLookup, staff);
    }

    private void filterSkillLookup(final LookupComponent skillLookup, final Entity staff) {
        FilterValueHolder filterValueHolder = skillLookup.getFilterValue();

        List<Long> skillIds = Lists.newArrayList();

        if (!Objects.isNull(staff)) {
            Optional<List<Long>> mayBeStaffIds = getStaffSkillIds(staff);

            if (mayBeStaffIds.isPresent()) {
                skillIds = mayBeStaffIds.get();
            }
        }

        if (skillIds.isEmpty()) {
            filterValueHolder.remove(L_SKILL_IDS);
        } else {
            filterValueHolder.put(L_SKILL_IDS, skillIds);
        }

        skillLookup.setFilterValue(filterValueHolder);
    }

    private Optional<List<Long>> getStaffSkillIds(final Entity staff) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT skill_id FROM basic_staffskill ");
		query.append("WHERE staff_id = :staffId");

        Map<String, Object> params = Maps.newHashMap();

        params.put("staffId", staff.getId());

        List<Long> skillIds;

        try {
            skillIds = jdbcTemplate.queryForList(query.toString(), params, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

        return Optional.of(skillIds);
    }

}
