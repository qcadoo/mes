package com.qcadoo.mes.materialFlow.hooks;

import org.springframework.stereotype.Component;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class TransformationViewHooks {

    public void disableExistingADLelements(final ViewDefinitionState view) {
        disableADL(view, "production");
        disableADL(view, "consumption");
    }

    private void disableADL(final ViewDefinitionState view, final String name) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(name);

        for (FormComponent form : adl.getFormComponents()) {
            disableFormIfQuantityIsntSet(form);
        }
    }

    private void disableFormIfQuantityIsntSet(final FormComponent form) {
        if (form.getEntity().getDecimalField("quantity") != null) {
            form.setFormEnabled(false);
        }
    }
}
