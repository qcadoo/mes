package com.qcadoo.mes.basic.listeners;

import com.qcadoo.mes.basic.constants.SkillFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class SkillListeners {

    private static final String SKILL = "skill";

    private static final String MAX_LEVEL = "maxLevel";

    public final void onOnSkillChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent skillComponentLookup = (LookupComponent) view
            .getComponentByReference(SKILL);

        Entity skill = skillComponentLookup.getEntity();

        if(Objects.nonNull(skill)) {
            FieldComponent maxSkillLevelField = (FieldComponent) view.getComponentByReference(MAX_LEVEL);
            maxSkillLevelField.setFieldValue(skill.getIntegerField(SkillFields.MAXIMUM_LEVEL));
        }
    }
}
