package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.techSubcontrForDeliveries.constants.TechSubcontrForDeliveriesConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
public class DeliveryDetailsListenersTSFDOverrideAspect {

    @Autowired
    private DeliveryDetailsListenersTSFDOverrideService deliveryDetailsListenersTSFDOverrideService;

    @Pointcut("execution(private com.qcadoo.model.api.Entity com.qcadoo.mes.deliveries.listeners.DeliveryDetailsListeners.copyOrderedProductToDelivered(com.qcadoo.model.api.Entity, java.util.List))")
    public void copyOrderedProductToDeliveredListenersExecution() {
    }

    @RunIfEnabled(TechSubcontrForDeliveriesConstants.PLUGIN_IDENTIFIER)
    @AfterReturning(value = "copyOrderedProductToDeliveredListenersExecution()", returning = "resultDelivery")
    public void afterCopyOrderedProductToDeliveredListenersExecution(final Entity resultDelivery) {
        deliveryDetailsListenersTSFDOverrideService.fillFieldWithOffer(resultDelivery);
    }

}