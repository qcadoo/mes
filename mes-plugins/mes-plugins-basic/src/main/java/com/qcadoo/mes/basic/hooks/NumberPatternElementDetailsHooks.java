package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.NumberPatternElement;
import com.qcadoo.mes.basic.constants.NumberPatternElementFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

import org.springframework.stereotype.Service;

@Service
public class NumberPatternElementDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FieldComponent value = (FieldComponent) view.getComponentByReference(NumberPatternElementFields.VALUE);
        FieldComponent element = (FieldComponent) view.getComponentByReference(NumberPatternElementFields.ELEMENT);
        if (NumberPatternElement.XX.getStringValue().equals(element.getFieldValue())) {
            value.setEnabled(true);
        } else {
            value.setEnabled(false);
            value.setFieldValue(null);
        }

        disableSequenceCycleForNotNumeric(view, element);
    }

    private void disableSequenceCycleForNotNumeric(ViewDefinitionState view, FieldComponent element) {
        FieldComponent sequenceCycle = (FieldComponent) view.getComponentByReference(NumberPatternElementFields.SEQUENCE_CYCLE);
        if (NumberPatternElement.N999.getStringValue().equals(element.getFieldValue())
                || NumberPatternElement.N9999.getStringValue().equals(element.getFieldValue())
                || NumberPatternElement.N99999.getStringValue().equals(element.getFieldValue())) {
            sequenceCycle.setEnabled(true);
        } else {
            sequenceCycle.setEnabled(false);
            sequenceCycle.setFieldValue(null);
        }
    }
}
