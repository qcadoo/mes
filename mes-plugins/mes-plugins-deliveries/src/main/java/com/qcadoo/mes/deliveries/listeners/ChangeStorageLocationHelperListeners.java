package com.qcadoo.mes.deliveries.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ChangeStorageLocationHelperListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public final void changeStorageLocation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity entity = form.getEntity();
        String ids = entity.getStringField("deliveredProductIds");

        String[] splitIds = ids.split(",");
        DataDefinition deliveredProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
        Entity storageLocation = entity.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);
        boolean success = true;
        for (String id : splitIds) {
            Long deliveredProductId = Long.parseLong(id);
            Entity deliveredProduct = deliveredProductDD.get(deliveredProductId);
            deliveredProduct.setField(DeliveredProductFields.STORAGE_LOCATION, storageLocation);
            Entity saved = deliveredProductDD.save(deliveredProduct);
            if (!saved.isValid()) {
                saved.getErrors().forEach((key, message) -> view.addMessage(message));
                saved.getGlobalErrors().forEach(message -> view.addMessage(message));
                success = false;
                break;
            }
        }
        if (success) {
            view.addMessage("deliveries.changeStorageLocationHelper.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("deliveries.changeStorageLocationHelper.error", ComponentState.MessageType.FAILURE);
        }
    }

}
