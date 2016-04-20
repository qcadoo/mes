package com.qcadoo.mes.technologies.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OPOCDetailsListeners {

    public void clearCheckbox(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        FieldComponent checkbox = (FieldComponent) view.getComponentByReference(OperationProductOutComponentFields.SET);
        checkbox.setFieldValue(false);
    }
}
