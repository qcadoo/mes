package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.basic.constants.SkillFields;
import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

import org.springframework.stereotype.Service;

@Service
public class SkillOperationDetailsHooks {

    private static final String L_FORM = "form";

    private static final String MAX_LEVEL = "maxLevel";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity opSkill = form.getEntity();
        FieldComponent maxSkillLevelField = (FieldComponent) view.getComponentByReference(MAX_LEVEL);
        maxSkillLevelField.setFieldValue(opSkill.getBelongsToField(OperationSkillFields.SKILL).getIntegerField(
                SkillFields.MAXIMUM_LEVEL));
    }
}
