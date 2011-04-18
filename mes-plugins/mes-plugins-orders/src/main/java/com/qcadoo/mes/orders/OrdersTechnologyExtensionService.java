package com.qcadoo.mes.orders;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

@Service
public class OrdersTechnologyExtensionService {

    public boolean checkExistingOrders(final DataDefinition dataDefinition, final Entity entity) {

        Entity newProduct = entity.getBelongsToField("product");
        EntityList orders = entity.getHasManyField("orders");
        if (orders == null) {
            return true;
        }

        for (Entity order : orders) {
            Entity orderProduct = order.getBelongsToField("product");

            if (!newProduct.getField("number").equals(orderProduct.getField("number"))) {
                entity.addError(dataDefinition.getField("product"), "orders.validate.global.error.technologyUsedInOrder");
                return false;
            }
        }
        return true;
    }
}
