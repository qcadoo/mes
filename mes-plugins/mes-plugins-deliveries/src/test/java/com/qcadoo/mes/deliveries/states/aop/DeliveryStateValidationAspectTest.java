/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.deliveries.states.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.Test;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.states.aop.listeners.DeliveryStateValidationAspect;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangePhase;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunForStateTransitions;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.plugin.api.RunIfEnabled;

public class DeliveryStateValidationAspectTest {

    private final Class<?> clazz = DeliveryStateValidationAspect.class;

    @Test
    public final void shouldBeAnnotatedWithAspectAndRunIfEnabledAnnotations() {
        assertEquals("com.qcadoo.mes.deliveries.states.aop.listeners.DeliveryStateValidationAspect", clazz.getCanonicalName());
        assertNotNull(clazz.getAnnotation(Aspect.class));
        RunIfEnabled runIfEnabled = clazz.getAnnotation(RunIfEnabled.class);
        assertNotNull(runIfEnabled);
        String runIfEnabledPliginIdentifier = runIfEnabled.value()[0];
        assertEquals(DeliveriesConstants.PLUGIN_IDENTIFIER, runIfEnabledPliginIdentifier);
    }

    @Test
    public final void shouldPreValidationOnReceivedMethodBeCorrectlyAnnotated() throws SecurityException, NoSuchMethodException {
        Method preValidationOnReceive = clazz.getMethod("preValidationOnReceived", StateChangeContext.class, int.class);
        assertNotNull(preValidationOnReceive);

        RunForStateTransitions transitions = preValidationOnReceive.getAnnotation(RunForStateTransitions.class);
        assertNotNull(transitions);
        assertEquals(2, transitions.value().length);

        RunForStateTransition firstTransition = transitions.value()[0];
        assertEquals(firstTransition.sourceState(), DeliveryStateStringValues.APPROVED);
        assertEquals(firstTransition.targetState(), DeliveryStateStringValues.RECEIVED);

        RunForStateTransition secondTransition = transitions.value()[1];
        assertEquals(secondTransition.sourceState(), DeliveryStateStringValues.APPROVED);
        assertEquals(secondTransition.targetState(), DeliveryStateStringValues.RECEIVE_CONFIRM_WAITING);

        RunInPhase runInPhase = preValidationOnReceive.getAnnotation(RunInPhase.class);
        assertNotNull(runInPhase);
        assertEquals(DeliveryStateChangePhase.PRE_VALIDATION, runInPhase.value()[0]);

        Before before = preValidationOnReceive.getAnnotation(Before.class);
        assertNotNull(before);
    }

    @Test
    public final void shouldPreValidationOnApprovedMethodBeCorrectlyAnnotated() throws SecurityException, NoSuchMethodException {
        Method preValidationOnReceive = clazz.getMethod("preValidationOnApproved", StateChangeContext.class, int.class);
        assertNotNull(preValidationOnReceive);
        RunForStateTransition transition = preValidationOnReceive.getAnnotation(RunForStateTransition.class);
        assertNotNull(transition);

        assertEquals(DeliveryStateStringValues.WILDCARD_STATE, transition.sourceState());
        assertEquals(DeliveryStateStringValues.APPROVED, transition.targetState());

        RunInPhase runInPhase = preValidationOnReceive.getAnnotation(RunInPhase.class);
        assertNotNull(runInPhase);
        assertEquals(DeliveryStateChangePhase.PRE_VALIDATION, runInPhase.value()[0]);

        Before before = preValidationOnReceive.getAnnotation(Before.class);
        assertNotNull(before);
    }

}
