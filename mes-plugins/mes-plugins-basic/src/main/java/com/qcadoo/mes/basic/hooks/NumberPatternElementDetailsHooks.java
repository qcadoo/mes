package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.NumberPatternElement;
import com.qcadoo.mes.basic.constants.NumberPatternElementFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

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
    }
}
