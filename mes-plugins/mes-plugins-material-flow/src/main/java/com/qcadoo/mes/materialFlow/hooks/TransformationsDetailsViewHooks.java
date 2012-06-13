package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.OPERATION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TIME;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.materialFlow.listeners.MultitransferListeners;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class TransformationsDetailsViewHooks {

    @Autowired
    private MultitransferListeners multitransferListeners;

    private static final List<String> FIELDS = Arrays.asList(TIME, STAFF, STOCK_AREAS_FROM, STOCK_AREAS_TO, OPERATION);

    public void disableExistingADLelements(final ViewDefinitionState view) {
        disableADL(view, "production");
        disableADL(view, "consumption");
    }

    private void disableADL(final ViewDefinitionState view, final String name) {
        FormComponent transformationsForm = (FormComponent) view.getComponentByReference("form");

        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(name);

        if (transformationsForm.getEntityId() == null) {
            adl.setEnabled(false);

            changeFieldComponentsState(view, true);
        } else {
            adl.setEnabled(true);

            changeFieldComponentsState(view, false);

            List<FormComponent> formComponents = adl.getFormComponents();

            for (FormComponent form : formComponents) {
                Entity transfer = form.getEntity();
                Entity product = transfer.getBelongsToField(PRODUCT);

                if (form.getEntity().isValid() && (form.getEntity().getDecimalField(QUANTITY) != null)
                        && !multitransferListeners.isProductAlreadyAdded(formComponents, product)) {
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
