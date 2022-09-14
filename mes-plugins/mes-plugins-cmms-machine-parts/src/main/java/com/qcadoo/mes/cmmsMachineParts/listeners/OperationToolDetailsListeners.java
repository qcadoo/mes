package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.stereotype.Service;

@Service
public class OperationToolDetailsListeners {

    public static final String L_TOOL = "tool";
    public static final String L_UNIT = "unit";

    public void onToolChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_UNIT);


        LookupComponent toolField = (LookupComponent) view.getComponentByReference(L_TOOL);
        if(toolField.isEmpty()) {
            unitField.setFieldValue(null);
            unitField.requestComponentUpdateState();
        } else {
            unitField.setFieldValue(toolField.getEntity().getStringField(L_UNIT));
            unitField.requestComponentUpdateState();
        }
    }


    public void onCategoryChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        LookupComponent toolField = (LookupComponent) view.getComponentByReference(L_TOOL);
        toolField.setFieldValue(null);
        toolField.requestComponentUpdateState();

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_UNIT);
        unitField.setFieldValue(null);
        unitField.requestComponentUpdateState();


    }


}