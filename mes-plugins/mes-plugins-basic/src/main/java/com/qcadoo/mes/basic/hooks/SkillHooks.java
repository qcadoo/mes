package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.SkillFields;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class SkillHooks {

    public static final String REQUIRED_LEVEL = "requiredLevel";

    public static final String OPERATION = "operation";

    public static final String NUMBER = "number";

    public boolean onValidate(final DataDefinition skillDD, final Entity skill) {
        if (Objects.isNull(skill.getId())) {
            return true;
        }

        List<Entity> staffSkills = skill.getHasManyField(SkillFields.STAFF_SKILLS).stream()
                .filter(ss -> ss.getIntegerField(StaffSkillsFields.LEVEL) > skill.getIntegerField(SkillFields.MAXIMUM_LEVEL))
                .collect(Collectors.toList());

        List<Entity> operationSkills = skill.getHasManyField(SkillFields.OPERATION_SKILLS).stream()
                .filter(ss -> ss.getIntegerField(REQUIRED_LEVEL) > skill.getIntegerField(SkillFields.MAXIMUM_LEVEL))
                .collect(Collectors.toList());

        if (!staffSkills.isEmpty() || !operationSkills.isEmpty()) {
            skill.addError(skill.getDataDefinition().getField(SkillFields.MAXIMUM_LEVEL),
                    "basic.skill.validation.error.canNotChangeMaximumLevel");
            if (!staffSkills.isEmpty()) {
                String staffs = staffSkills.stream()
                        .map(ops -> ops.getBelongsToField(StaffSkillsFields.STAFF).getStringField(StaffFields.NUMBER))
                        .collect(Collectors.joining(", "));
                skill.addGlobalError("basic.skill.validation.error.canNotChangeMaximumLevelStaffs", false, staffs);
            }

            if (!operationSkills.isEmpty()) {
                String operations = operationSkills.stream()
                        .map(ops -> ops.getBelongsToField(OPERATION).getStringField(NUMBER))
                        .collect(Collectors.joining(", "));
                skill.addGlobalError("basic.skill.validation.error.canNotChangeMaximumLevelOperations", false, operations);

            }
            return false;
        }
        return true;
    }
}
