package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingOperationProductInComponentDetailsHooksPFTD {

    public void onBeforeRender(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity topic = form.getEntity();
        Entity topicDb = topic.getDataDefinition().get(topic.getId());
        List<Entity> resourceReservations = topicDb.getHasManyField("resourceReservations");
        if (resourceReservations.isEmpty()) {
            ComponentState resourcesTab = view.getComponentByReference("resourcesTab");
            resourcesTab.setVisible(false);
        }
    }

}