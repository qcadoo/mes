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

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SkillFields;
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.mes.technologies.constants.OperationSkillsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationSkillHooks {

    public boolean validatesWith(final DataDefinition operationSkillDD, final Entity operationSkill) {
        boolean isValid = checkIfRequiredLevelIsValid(operationSkillDD, operationSkill);

        return isValid;
    }

    private boolean checkIfRequiredLevelIsValid(final DataDefinition operationSkillDD, final Entity operationSkill) {
        boolean isValid = true;

        Entity skill = operationSkill.getBelongsToField(StaffSkillsFields.SKILL);
        Integer requiredLevel = operationSkill.getIntegerField(OperationSkillsFields.REQUIRED_LEVEL);

        if (!Objects.isNull(skill) && !Objects.isNull(requiredLevel)) {
            Integer maximumLevel = skill.getIntegerField(SkillFields.MAXIMUM_LEVEL);

            if (requiredLevel.compareTo(maximumLevel) > 0) {
                operationSkill.addError(operationSkillDD.getField(OperationSkillsFields.REQUIRED_LEVEL), "technologies.operationSkill.error.requiredLevel.greaterThanMaximumLevel");

                isValid = false;
            }
        }

        return isValid;
    }

}
