package com.qcadoo.mes.states.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.qcadoo.mes.states.messages.constants.MessageType;

/**
 * Execute annotated method only if given state change entity does not have any messages of type {@link MessageType#FAILURE}.
 * 
 * @since 1.1.7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface StateChangePhase {

}
