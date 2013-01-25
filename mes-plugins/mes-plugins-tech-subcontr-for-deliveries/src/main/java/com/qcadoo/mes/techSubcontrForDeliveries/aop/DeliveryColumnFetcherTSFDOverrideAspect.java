package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.model.api.Entity;

@Aspect
@Configurable
public class DeliveryColumnFetcherTSFDOverrideAspect {

    @Autowired
    private DeliveryColumnFetcherTSFDOverrideUtil deliveryColumnFetcherTSFDOverrideUtil;

    @Pointcut("execution(private boolean com.qcadoo.mes.deliveries.print.DeliveryColumnFetcher.compareProducts(..)) "
            + "&& args(deliveryProduct, deliveredProduct)")
    public void compareProductsExecution(final DeliveryProduct deliveryProduct, final Entity deliveredProduct) {
    }

    @Around("compareProductsExecution(deliveryProduct, deliveredProduct)")
    public boolean aroundCompareProductsExecution(final ProceedingJoinPoint pjp, final DeliveryProduct deliveryProduct,
            final Entity deliveredProduct) throws Throwable {
        if (deliveryColumnFetcherTSFDOverrideUtil.shouldOverride()) {
            return deliveryColumnFetcherTSFDOverrideUtil.compareProductsAndOperation(deliveryProduct, deliveredProduct);
        } else {
            return (Boolean) pjp.proceed();
        }
    }

}
