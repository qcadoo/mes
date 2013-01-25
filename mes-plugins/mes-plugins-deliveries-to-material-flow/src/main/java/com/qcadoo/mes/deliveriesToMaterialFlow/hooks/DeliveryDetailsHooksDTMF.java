package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveryFieldsDTMF.LOCATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
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

}
