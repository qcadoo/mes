package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.LOCATION;

import org.springframework.stereotype.Component;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

@Component
public class StockCorrectionDetailsHooks {

    public void checkIfLocationHasExternalNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        checkIfLocationHasExternalNumber(view);
    }

    public void checkIfLocationHasExternalNumber(final ViewDefinitionState view) {
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(LOCATION);
        Entity location = locationLookup.getEntity();
        if (location != null) {
            if (location.getStringField("externalNumber") != null) {
                locationLookup.addMessage("materialFlow.validate.global.error.locationHasExternalNumber",
                        ComponentState.MessageType.FAILURE);
            }
        }
    }

}
