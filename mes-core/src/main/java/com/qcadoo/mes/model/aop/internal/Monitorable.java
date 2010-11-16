/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.aop.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes monitorable attributes on a method. Every call of this method will be logged into log file (method name, parameters
 * and the time of execution).
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitorable {

    /**
     * Number of milliseconds over which the warn is logged.
     * 
     * @return time threshold
     */
    long threshold() default 100;

}
