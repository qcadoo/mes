package com.qcadoo.mes.states.annotation;

import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface RunForStateTransition {

    String sourceState() default WILDCARD_STATE;

    String targetState() default WILDCARD_STATE;
}
