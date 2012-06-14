package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.OPERATION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class TransformationsDetailsViewHooks {

    private static final String L_FORM = "form";

    private static final List<String> FIELDS = Arrays.asList(TIME, STAFF, STOCK_AREAS_FROM, STOCK_AREAS_TO, OPERATION);

    public void disableExistingADLelements(final ViewDefinitionState view) {
        disableADL(view, TRANSFERS_CONSUMPTION);
        disableADL(view, TRANSFERS_PRODUCTION);
    }

    private void disableADL(final ViewDefinitionState view, final String name) {
        FormComponent transformationsForm = (FormComponent) view.getComponentByReference(L_FORM);

        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(name);

        if (transformationsForm.getEntityId() == null) {
            adl.setEnabled(false);

            changeFieldComponentsState(view, true);
        } else {
            adl.setEnabled(true);

            changeFieldComponentsState(view, false);

            List<FormComponent> formComponents = adl.getFormComponents();

            for (FormComponent form : formComponents) {
                if ((form.getEntityId() != null) && form.getEntity().isValid()) {
                    form.setFormEnabled(false);
                } else {
                    form.setFormEnabled(true);
                }
            }
        }
    }

    private void changeFieldComponentsState(final ViewDefinitionState view, final boolean isEnabled) {
        for (String fieldName : FIELDS) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldName);

            if (OPERATION.equals(fieldName)) {
                fieldComponent.setEnabled(!isEnabled);
            } else {
                fieldComponent.setEnabled(isEnabled);
            }
            fieldComponent.requestComponentUpdateState();
        }
    }
}
