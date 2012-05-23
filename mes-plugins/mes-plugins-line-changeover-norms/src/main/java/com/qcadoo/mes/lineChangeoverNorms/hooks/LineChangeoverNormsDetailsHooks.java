package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.CHANGEOVER_TYPE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.constants.ChangeoverType;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class LineChangeoverNormsDetailsHooks {

    public void invisibleAndSetRequiredFields(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        invisibleField(viewDefinitionState);
    }

    public void invisibleField(final ViewDefinitionState viewDefinitionState) {
        FieldComponent changeoverType = (FieldComponent) viewDefinitionState.getComponentByReference(CHANGEOVER_TYPE);
        boolean selectForTechnology = changeoverType.getFieldValue().equals(ChangeoverType.FOR_TECHNOLOGY.getStringValue());

        for (String reference : Arrays.asList(FROM_TECHNOLOGY, TO_TECHNOLOGY)) {
            FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(reference);
            field.setVisible(selectForTechnology);
            field.setRequired(selectForTechnology);
            field.requestComponentUpdateState();
        }

        for (String reference : Arrays.asList(FROM_TECHNOLOGY_GROUP, TO_TECHNOLOGY_GROUP)) {
            FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(reference);
            field.setVisible(!selectForTechnology);
            field.setRequired(!selectForTechnology);
            field.requestComponentUpdateState();
        }
    }

}
