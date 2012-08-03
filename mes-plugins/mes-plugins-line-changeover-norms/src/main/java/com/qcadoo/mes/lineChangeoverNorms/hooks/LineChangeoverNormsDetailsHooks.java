package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.ChangeoverType.FOR_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.CHANGEOVER_TYPE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class LineChangeoverNormsDetailsHooks {

    private static final List<String> TECHNOLOGY_FIELDS = Arrays.asList(FROM_TECHNOLOGY, TO_TECHNOLOGY);

    private static final List<String> TECHNOLOGY_GROUP_FIELDS = Arrays.asList(FROM_TECHNOLOGY_GROUP, TO_TECHNOLOGY_GROUP);

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        setFieldsVisibleAndRequired(view);
    }

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view) {
        FieldComponent changeoverType = (FieldComponent) view.getComponentByReference(CHANGEOVER_TYPE);

        boolean selectForTechnology = FOR_TECHNOLOGY.getStringValue().equals(changeoverType.getFieldValue());

        changeFieldsState(view, TECHNOLOGY_FIELDS, selectForTechnology);
        changeFieldsState(view, TECHNOLOGY_GROUP_FIELDS, !selectForTechnology);
    }

    private void changeFieldsState(final ViewDefinitionState view, final List<String> fieldNames,
            final boolean selectForTechnology) {
        for (String fieldName : fieldNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(fieldName);
            field.setVisible(selectForTechnology);
            field.setRequired(selectForTechnology);

            if (!selectForTechnology) {
                field.setFieldValue(null);
            }

            field.requestComponentUpdateState();
        }
    }

}
