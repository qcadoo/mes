package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.DeliveredProductFieldsTSFD;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.OrderedProductFieldsTSFD;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveryDetailsListenersTSFDOverrideService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillFieldWithOffer(final Entity delivery) {
        final List<Entity> deliveredProductList = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        for (Entity deliveredProduct : deliveredProductList) {
            final Entity orderedProduct = getOrderProduct(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT),
                    delivery);
            deliveredProduct.setField(DeliveredProductFieldsTSFD.OPERATION,
                    orderedProduct.getBelongsToField(OrderedProductFieldsTSFD.OPERATION));
        }
        delivery.getDataDefinition().save(delivery);
    }

    private Entity getOrderProduct(final Entity product, final Entity delivery) {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT).find()
                .add(SearchRestrictions.belongsTo("product", product))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY, delivery)).uniqueResult();
    }

}