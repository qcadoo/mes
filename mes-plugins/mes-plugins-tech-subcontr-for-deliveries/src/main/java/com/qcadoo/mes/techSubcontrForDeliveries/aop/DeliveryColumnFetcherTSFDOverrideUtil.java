package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.DeliveredProductFieldsTSFD;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.OrderedProductFieldsTSFD;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.TechSubcontrForDeliveriesConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginStateResolver;

@Service
public class DeliveryColumnFetcherTSFDOverrideUtil {

    @Autowired
    private PluginStateResolver pluginStateResolver;

    @Autowired
    private DeliveriesService deliveriesService;

    public boolean shouldOverride() {
        return pluginStateResolver.isEnabled(TechSubcontrForDeliveriesConstants.PLUGIN_IDENTIFIER);
    }

    public boolean containsOrderedWithProductAndOperation(final DeliveryProduct deliveryProduct, final Entity deliveredProduct) {
        Entity operation = getOperation(deliveryProduct);
        Entity product = getProduct(deliveryProduct);
        Entity deliveredOperation = deliveredProduct.getBelongsToField(DeliveredProductFieldsTSFD.OPERATION);

        boolean haveEqualsProduct = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getId()
                .equals(product.getId());

        return haveEqualsProduct && checkIfHaveNotOperationOrHaveTheSameOperation(operation, deliveredOperation);
    }

    private boolean checkIfHaveNotOperationOrHaveTheSameOperation(final Entity operation, final Entity deliveredOperation) {
        return (operation == null && deliveredOperation == null)
                || ((operation != null) && (deliveredOperation != null) && operation.equals(deliveredOperation));
    }

    private Entity getOperation(final DeliveryProduct deliveryProduct) {
        if (deliveryProduct.getOrderedProductId() != null) {
            Entity orderedProduct = deliveriesService.getOrderedProduct(deliveryProduct.getOrderedProductId());
            return orderedProduct.getBelongsToField(OrderedProductFieldsTSFD.OPERATION);
        } else {
            Entity deliveredProductEntity = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());
            return deliveredProductEntity.getBelongsToField(DeliveredProductFieldsTSFD.OPERATION);
        }
    }

    private Entity getProduct(final DeliveryProduct deliveryProduct) {
        if (deliveryProduct.getOrderedProductId() != null) {
            Entity orderedProduct = deliveriesService.getOrderedProduct(deliveryProduct.getOrderedProductId());
            return orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        } else {
            Entity deliveredProductEntity = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());
            return deliveredProductEntity.getBelongsToField(DeliveredProductFields.PRODUCT);
        }
    }

}
