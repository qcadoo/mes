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

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SkillFields;
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StaffSkillHooks {

    public boolean validatesWith(final DataDefinition staffSkillDD, final Entity staffSkill) {
        boolean isValid = checkIfLevelIsValid(staffSkillDD, staffSkill);

        return isValid;
    }

    private boolean checkIfLevelIsValid(final DataDefinition staffSkillDD, final Entity staffSkill) {
        boolean isValid = true;

        Entity skill = staffSkill.getBelongsToField(StaffSkillsFields.SKILL);
        Integer level = staffSkill.getIntegerField(StaffSkillsFields.LEVEL);

        if (!Objects.isNull(skill) && !Objects.isNull(level)) {
            Integer maximumLevel = skill.getIntegerField(SkillFields.MAXIMUM_LEVEL);

            if (level.compareTo(maximumLevel) > 0) {
                staffSkill.addError(staffSkillDD.getField(StaffSkillsFields.LEVEL), "basic.staffSkill.error.level.greaterThanMaximumLevel");

                isValid = false;
            }
        }

        return isValid;
    }

}
