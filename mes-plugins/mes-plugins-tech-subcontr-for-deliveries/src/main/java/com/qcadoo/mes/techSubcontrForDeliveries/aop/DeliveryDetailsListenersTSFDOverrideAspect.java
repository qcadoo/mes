package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.DeliveredProductFieldsTSFD;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.OrderedProductFieldsTSFD;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Aspect
@Configurable
public class DeliveryDetailsListenersTSFDOverrideAspect {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Pointcut("execution(private com.qcadoo.model.api.Entity com.qcadoo.mes.deliveries.listeners.DeliveryDetailsListeners.copyOrderedProductToDelivered(..)) "
            + "&& args(delivery, orderedProducts)")
    public void copyOrderedProductToDeliveredListenersExecution(final Entity delivery, final List<Entity> orderedProducts) {
    }

    @AfterReturning(value = "copyOrderedProductToDeliveredListenersExecution(delivery, orderedProducts)", returning = "deliveryEntity")
    public void afterCopyOrderedProductToDeliveredListenersExecution(final ProceedingJoinPoint pjp, final Entity delivery,
            final List<Entity> orderedProducts, final Entity deliveryEntity) throws Throwable {
        fillFieldWithOffer(deliveryEntity, orderedProducts);
    }

    private void fillFieldWithOffer(final Entity delivery, final List<Entity> orderedProducts) {
        List<Entity> deliveredProductList = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        for (Entity deliveredProduct : deliveredProductList) {
            Entity orderedProduct = getOrderProduct(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT), delivery);
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