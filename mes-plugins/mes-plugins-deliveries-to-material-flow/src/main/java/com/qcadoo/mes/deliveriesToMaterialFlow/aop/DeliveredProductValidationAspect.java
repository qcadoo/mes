package com.qcadoo.mes.deliveriesToMaterialFlow.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveriesToMaterialFlow.aop.helper.DeliveredProductValidationHelper;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveriesToMaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(DeliveriesToMaterialFlowConstants.PLUGIN_IDENTIFIER)
public class DeliveredProductValidationAspect {

    private static Logger LOG = LoggerFactory.getLogger(DeliveredProductValidationAspect.class);

    @Autowired
    DeliveredProductValidationHelper deliveredProductValidaionHelper;

    @Pointcut("execution(public boolean com.qcadoo.mes.deliveries.hooks.DeliveredProductHooks.checkIfDeliveredProductAlreadyExists(..))"
            + "&& args(deliveredProductDD, deliveredProduct)")
    public void checkIfDeliveredProductExistsWithBatchParameters(final DataDefinition deliveredProductDD,
            final Entity deliveredProduct) {
    }

    @Around("checkIfDeliveredProductExistsWithBatchParameters(deliveredProductDD, deliveredProduct)")
    public boolean aroundCheckIfDeliveredProductExistsWithBatchParameters(final ProceedingJoinPoint pjp,
            final DataDefinition deliveredProductDD, final Entity deliveredProduct) throws Throwable {
        LOG.debug("Around checkIfDeliveredProductExistsWithBatchParameters");
        return deliveredProductValidaionHelper.checkDeliveredProductUniqueness(deliveredProductDD, deliveredProduct);
    }

}
