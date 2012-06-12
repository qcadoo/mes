package com.qcadoo.mes.materialFlow.hooks;

import org.springframework.stereotype.Component;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class TransformationsDetailsViewHooks {

    public void disableExistingADLelements(final ViewDefinitionState view) {
        disableADL(view, "production");
        disableADL(view, "consumption");
    }

    private void disableADL(final ViewDefinitionState view, final String name) {
        FormComponent transformationsForm = (FormComponent) view.getComponentByReference("form");

        if (transformationsForm.getEntityId() != null) {
            AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(name);

            for (FormComponent form : adl.getFormComponents()) {
                disableFormIfIsValid(form);
            }
        }
    }

    private void disableFormIfIsValid(final FormComponent form) {
        if (form.getEntity().isValid()) {
            form.setFormEnabled(false);
        }
    }
}
