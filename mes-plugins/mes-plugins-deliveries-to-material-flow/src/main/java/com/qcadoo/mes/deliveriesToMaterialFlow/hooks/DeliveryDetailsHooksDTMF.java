package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.STATE;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.DECLINED;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.RECEIVED;
import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveryFieldsDTMF.LOCATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class DeliveryDetailsHooksDTMF {

    private static final String L_FORM = "form";

    @Autowired
    private ParameterService parameterService;

    public void fillLocationDefaultValue(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (deliveryForm.getEntityId() != null) {
            return;
        }

        LookupComponent locationField = (LookupComponent) view.getComponentByReference(LOCATION);
        Entity location = locationField.getEntity();

        if (location == null) {
            Entity defaultLocation = parameterService.getParameter().getBelongsToField(LOCATION);

            if (defaultLocation != null) {
                locationField.setFieldValue(defaultLocation.getId());
                locationField.requestComponentUpdateState();
            }
        }
    }

    public void changeLocationEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent stateField = (FieldComponent) view.getComponentByReference(STATE);
        String state = stateField.getFieldValue().toString();

        LookupComponent locationField = (LookupComponent) view.getComponentByReference(LOCATION);

        if (deliveryForm.getEntityId() == null) {
            locationField.setEnabled(true);
        } else {
            if (DECLINED.getStringValue().equals(state) || RECEIVED.getStringValue().equals(state)) {
                locationField.setEnabled(false);
            } else {
                locationField.setEnabled(true);
            }
        }
    }

}
