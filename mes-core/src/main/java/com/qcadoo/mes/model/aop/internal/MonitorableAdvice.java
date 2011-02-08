/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

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
public final class MonitorableAdvice {

    private static final Logger PERFORMANCE_LOG = LoggerFactory.getLogger("PERFORMANCE");

    @Around("@annotation(monitorable)")
    public Object doBasicProfiling(final ProceedingJoinPoint pjp, final Monitorable monitorable) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return pjp.proceed();
        } finally {
            long end = System.currentTimeMillis();
            long difference = end - start;

            if (difference > monitorable.threshold()) {
                PERFORMANCE_LOG.warn("Call " + pjp.getSignature().toShortString() + " with " + Arrays.toString(pjp.getArgs()) + " took "
                        + difference + " ms ");
            } else if (PERFORMANCE_LOG.isDebugEnabled()) {
                PERFORMANCE_LOG.debug("Call " + pjp.getSignature().toShortString() + " with " + Arrays.toString(pjp.getArgs()) + " took "
                        + difference + " ms ");
            }

        }
    }
}