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
package com.qcadoo.mes.basic.validators;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SkillFields;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StaffSkillValidators {

    public boolean validatesWith(final DataDefinition staffSkillDD, final Entity staffSkill) {
        boolean isValid = true;

		isValid = checkIfSkillIsNotAlreadyAdded(staffSkillDD, staffSkill) && isValid;
        isValid = checkIfLevelIsValid(staffSkillDD, staffSkill) && isValid;

        return isValid;
    }

    private boolean checkIfSkillIsNotAlreadyAdded(final DataDefinition staffSkillDD, final Entity staffSkill) {
        boolean isNotAlreadyAdded = true;

		Long staffSkillId = staffSkill.getId();
        Entity staff = staffSkill.getBelongsToField(StaffSkillsFields.STAFF);
        Entity skill = staffSkill.getBelongsToField(StaffSkillsFields.SKILL);

        if (!Objects.isNull(staff) && !Objects.isNull(skill)) {
            Entity staffFromDB = staff.getDataDefinition().get(staff.getId());

            List<Entity> staffSkills = staffFromDB.getHasManyField(StaffFields.STAFF_SKILLS);

			if (!Objects.isNull(staffSkillId)) {
				staffSkills = filterCurrentSkill(staffSkill, staffSkills);
			}

            if (checkIfSkillIsAlreadyAdded(skill, staffSkills)) {
                staffSkill.addError(staffSkillDD.getField(StaffSkillsFields.SKILL), "basic.staffSkill.error.skill.alreadyAdded");

                isNotAlreadyAdded = false;
            }
        }

        return isNotAlreadyAdded;
    }

	private List<Entity> filterCurrentSkill(final Entity staffSkill, final List<Entity> staffSkills) {
		return staffSkills.stream().filter(addedStaffSkill -> !addedStaffSkill.getId().equals(staffSkill.getId())).collect(Collectors.toList());
	}

	private boolean checkIfSkillIsAlreadyAdded(final Entity skill, final List<Entity> staffSkills) {
		return staffSkills.stream().anyMatch(addedStaffSkill -> addedStaffSkill.getBelongsToField(StaffSkillsFields.SKILL).getId().equals(skill.getId()));
	}

	private boolean checkIfLevelIsValid(final DataDefinition staffSkillDD, final Entity staffSkill) {
		boolean isValid = true;

		Entity skill = staffSkill.getBelongsToField(StaffSkillsFields.SKILL);
		Integer level = staffSkill.getIntegerField(StaffSkillsFields.LEVEL);

		if (!Objects.isNull(skill) && !Objects.isNull(level)) {
			Integer maximumLevel = skill.getIntegerField(SkillFields.MAXIMUM_LEVEL);

			if (level.compareTo(maximumLevel) > 0) {
				staffSkill.addError(staffSkillDD.getField(StaffSkillsFields.LEVEL),
						"basic.staffSkill.error.level.greaterThanMaximumLevel");

				isValid = false;
			}
		}

		return isValid;
	}

}
