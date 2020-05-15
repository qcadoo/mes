package com.qcadoo.mes.deliveries.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ChangeStorageLocationHelperListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public final void changeStorageLocation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getEntity();
        String ids = entity.getStringField("deliveredProductIds");

        String[] splitIds = ids.split(",");
        DataDefinition deliveredProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
        Entity storageLocation = entity.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);
        List<String> changedProducts = Lists.newArrayList();
        List<String> failedProducts = Lists.newArrayList();
        for (String id : splitIds) {
            Long deliveredProductId = Long.parseLong(id);
            Entity deliveredProduct = deliveredProductDD.get(deliveredProductId);
            deliveredProduct.setField(DeliveredProductFields.STORAGE_LOCATION, storageLocation);
            deliveredProduct.setField(DeliveredProductFields.VALIDATE_PALLET, false);
            Entity saved = deliveredProductDD.save(deliveredProduct);
            String productNumber = saved.getBelongsToField(DeliveredProductFields.PRODUCT).getStringField(ProductFields.NUMBER);
            if (saved.isValid()) {
                changedProducts.add(productNumber);
            } else {
                failedProducts.add(productNumber);
                saved.getErrors().forEach((key, message) -> view.addMessage(message));
                saved.getGlobalErrors().forEach(view::addMessage);
            }
        }
        if (!changedProducts.isEmpty()) {
            view.addMessage("deliveries.changeStorageLocationHelper.success", ComponentState.MessageType.SUCCESS, false,
                    String.join(", ", changedProducts));
        }
        if (!failedProducts.isEmpty()) {
            view.addMessage("deliveries.changeStorageLocationHelper.error", ComponentState.MessageType.FAILURE, false,
                    String.join(", ", failedProducts));
        }
    }

}
