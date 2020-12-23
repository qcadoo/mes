package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.basic.constants.SkillFields;
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SkillOperationDetailsHooks {

    private static final String MAX_LEVEL = "maxLevel";

    private static final String L_SKILL_ID = "skillId";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity opSkill = form.getEntity();
        FieldComponent maxSkillLevelField = (FieldComponent) view.getComponentByReference(MAX_LEVEL);
        maxSkillLevelField.setFieldValue(opSkill.getBelongsToField(OperationSkillFields.SKILL).getIntegerField(
                SkillFields.MAXIMUM_LEVEL));

        LookupComponent operationLookup = (LookupComponent) view.getComponentByReference(OperationSkillFields.OPERATION);

        FilterValueHolder filterValueHolder = operationLookup.getFilterValue();

        Long skillId = opSkill.getBelongsToField(StaffSkillsFields.SKILL).getId();

        if (Objects.isNull(skillId)) {
            filterValueHolder.remove(L_SKILL_ID);
        } else {
            filterValueHolder.put(L_SKILL_ID, skillId);
        }

        operationLookup.setFilterValue(filterValueHolder);
    }
}
