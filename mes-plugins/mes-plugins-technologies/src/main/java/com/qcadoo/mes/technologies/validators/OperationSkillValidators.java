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
package com.qcadoo.mes.technologies.validators;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SkillFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationSkillValidators {

    public boolean validatesWith(final DataDefinition operationSkillDD, final Entity operationSkill) {
        boolean isValid = true;

		isValid = checkIfSkillIsNotAlreadyAdded(operationSkillDD, operationSkill) && isValid;
        isValid = checkIfRequiredLevelIsValid(operationSkillDD, operationSkill) && isValid;

        return isValid;
    }

	private boolean checkIfSkillIsNotAlreadyAdded(final DataDefinition operationSkillDD, final Entity operationSkill) {
		boolean isNotAlreadyAdded = true;

		Long operationSkillId = operationSkill.getId();
		Entity operation = operationSkill.getBelongsToField(OperationSkillFields.OPERATION);
		Entity skill = operationSkill.getBelongsToField(OperationSkillFields.SKILL);

		if (!Objects.isNull(operation) && !Objects.isNull(skill)) {
			Entity operationFromDB = operation.getDataDefinition().get(operation.getId());

			List<Entity> operationSkills = operationFromDB.getHasManyField(OperationFields.OPERATION_SKILLS);

			if (!Objects.isNull(operationSkillId)) {
				operationSkills = filterCurrentSkill(operationSkill, operationSkills);
			}

			if (checkIfSkillIsAlreadyAdded(skill, operationSkills)) {
				operationSkill.addError(operationSkillDD.getField(OperationSkillFields.SKILL),
						"technologies.operationSkill.error.skill.alreadyAdded");

				isNotAlreadyAdded = false;
			}
		}

		return isNotAlreadyAdded;
	}

	private List<Entity> filterCurrentSkill(final Entity operationSkill, final List<Entity> operationSkills) {
		return operationSkills.stream().filter(addedOperationSkill -> !addedOperationSkill.getId().equals(operationSkill.getId())).collect(Collectors.toList());
	}

	private boolean checkIfSkillIsAlreadyAdded(final Entity skill, final List<Entity> operationSkills) {
		return operationSkills.stream().anyMatch(addedOperationSkill -> addedOperationSkill.getBelongsToField(OperationSkillFields.SKILL).getId().equals(skill.getId()));
	}

    private boolean checkIfRequiredLevelIsValid(final DataDefinition operationSkillDD, final Entity operationSkill) {
        boolean isValid = true;

        Entity skill = operationSkill.getBelongsToField(OperationSkillFields.SKILL);
        Integer requiredLevel = operationSkill.getIntegerField(OperationSkillFields.REQUIRED_LEVEL);

        if (!Objects.isNull(skill) && !Objects.isNull(requiredLevel)) {
            Integer maximumLevel = skill.getIntegerField(SkillFields.MAXIMUM_LEVEL);

            if (requiredLevel.compareTo(maximumLevel) > 0) {
                operationSkill.addError(operationSkillDD.getField(OperationSkillFields.REQUIRED_LEVEL),
                        "technologies.operationSkill.error.requiredLevel.greaterThanMaximumLevel");

                isValid = false;
            }
        }

        return isValid;
    }

}
