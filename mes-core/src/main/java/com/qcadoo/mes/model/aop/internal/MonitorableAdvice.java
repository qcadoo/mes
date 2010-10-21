package com.qcadoo.mes.model.aop.internal;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MonitorableAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorableAdvice.class);

    @Around("@annotation(monitorable)")
    public Object doBasicProfiling(final ProceedingJoinPoint pjp, final Monitorable monitorable) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return pjp.proceed();
        } finally {
            long end = System.currentTimeMillis();
            long difference = end - start;

            if (difference > monitorable.threshold()) {
                LOG.warn("Call " + pjp.getSignature().toShortString() + " with " + Arrays.toString(pjp.getArgs()) + " took "
                        + difference + " ms ");
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Call " + pjp.getSignature().toShortString() + " with " + Arrays.toString(pjp.getArgs()) + " took "
                        + difference + " ms ");
            }

        }
    }
}