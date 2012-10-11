package com.qcadoo.mes.deliveries.listeners;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class SupplyItemsListeners {

    public void redirectToDeliveryDetails(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        if (grid.getSelectedEntities().isEmpty()) {
            return;
        }
        Entity orderedProducts = grid.getSelectedEntities().get(0);
        Entity delivery = orderedProducts.getBelongsToField(DeliveredProductFields.DELIVERY);
        if (delivery == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", delivery.getId());

        String url = "../page/deliveries/deliveryDetails.html";
        view.redirectTo(url, false, true, parameters);
    }
}
