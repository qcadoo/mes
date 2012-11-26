package com.qcadoo.mes.materialFlow.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class LocationDetailsViewHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_FORM = "form";

    public void disableLocationFormForExternalItems(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }

        Entity entity = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION)
                .get(form.getEntityId());

        if (entity == null) {
            return;
        }

        String externalNumber = entity.getStringField("externalNumber");

        if (externalNumber != null) {
            form.setFormEnabled(false);
        }

    }
}
