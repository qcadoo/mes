package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.stereotype.Service;

import static com.qcadoo.mes.technologies.constants.TechnologicalProcessFields.*;

@Service
public class TechnologicalProcessComponentDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        CheckBoxComponent extendedTimeForSizeGroupField = (CheckBoxComponent) view
                .getComponentByReference(EXTENDED_TIME_FOR_SIZE_GROUP);
        FieldComponent increasePercentField = (FieldComponent) view.getComponentByReference(INCREASE_PERCENT);
        LookupComponent sizeGroupLookup = (LookupComponent) view.getComponentByReference(SIZE_GROUP);

        if (extendedTimeForSizeGroupField.isChecked()) {
            increasePercentField.setEnabled(true);
            increasePercentField.setRequired(true);
            sizeGroupLookup.setEnabled(true);
            sizeGroupLookup.setRequired(true);
        } else {
            increasePercentField.setEnabled(false);
            increasePercentField.setFieldValue(null);
            increasePercentField.setRequired(false);
            sizeGroupLookup.setEnabled(false);
            sizeGroupLookup.setFieldValue(null);
            sizeGroupLookup.setRequired(false);
        }
        increasePercentField.requestComponentUpdateState();
        sizeGroupLookup.requestComponentUpdateState();
    }
}
