/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.states.aop;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.states.MockStateChangeDescriber;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateChangeTest;
import com.qcadoo.mes.states.annotation.StateChangePhase;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class StateChangePhaseAspectTest extends StateChangeTest {

    private static final String TOUCHED_FIELD = "touched";

    private static final String TOUCHED_PHASE = "touchedPhase";

    private TestStateChangeService stateChangeService;

    @Test
    public final void checkPointcutsDefinition() throws NoSuchMethodException {
        assertEquals("com.qcadoo.mes.states.annotation.StateChangePhase", StateChangePhase.class.getCanonicalName());
        assertEquals("com.qcadoo.mes.states.StateChangeContext", StateChangeContext.class.getCanonicalName());
        assertEquals("com.qcadoo.mes.states.service.StateChangeService", StateChangeService.class.getCanonicalName());

        final Map<Integer, Class<?>> changeStateArgsMap = Maps.newHashMap();
        changeStateArgsMap.put(0, StateChangeContext.class);
        final Method changeStateMethod = findMethod(StateChangeService.class, "changeState", changeStateArgsMap);
        assertNotNull(changeStateMethod);
        Assert.assertTrue(Modifier.isPublic(changeStateMethod.getModifiers()));
        assertEquals(void.class, changeStateMethod.getReturnType());

        final Map<Integer, Class<?>> changeStatePhaseArgsMap = changeStateArgsMap;
        final Method changeStatePhaseMethod = findMethod(StateChangeService.class, "changeState", changeStatePhaseArgsMap);
        assertNotNull(changeStatePhaseMethod);
        Assert.assertTrue(Modifier.isPublic(changeStatePhaseMethod.getModifiers()));
        assertEquals(void.class, changeStatePhaseMethod.getReturnType());
    }

    private Method findMethod(final Class<?> clazz, final String name, final Map<Integer, Class<?>> argsMap) {
        final List<Method> methods = Lists.newArrayList(clazz.getMethods());
        methods.addAll(Lists.newArrayList(clazz.getDeclaredMethods()));
        for (Method method : methods) {
            if (method.getName().equals(name) && methodMatchArguments(method, argsMap)) {
                return method;
            }
        }
        Assert.fail("method not found");
        return null;
    }

    private boolean methodMatchArguments(final Method method, final Map<Integer, Class<?>> argsMap) {
        final Class<?>[] arguments = method.getParameterTypes();
        for (Map.Entry<Integer, Class<?>> argEntry : argsMap.entrySet()) {
            final int argIndex = argEntry.getKey();
            final Class<?> argType = argEntry.getValue();
            if (!argType.isAssignableFrom(arguments[argIndex])) {
                return false;
            }
        }
        return true;
    }

    @Aspect
    public static class TestStateChangeService extends AbstractStateChangeAspect {

        @Pointcut("this(TestStateChangeService)")
        public void selectorPointcut() {
        }

        @Override
        public void changeState(final StateChangeContext stateChangeContext) {
            final Entity stateChangeEntity = stateChangeContext.getStateChangeEntity();
            stateChangeEntity.setField(TOUCHED_FIELD, true);
        }

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

        @Override
        protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
            final Entity stateChangeEntity = stateChangeContext.getStateChangeEntity();
            stateChangeEntity.setField(TOUCHED_PHASE, phaseNumber);
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        stateChangeService = new TestStateChangeService();
        stubStateChangeEntity(DESCRIBER);
        stubStateChangeContext();
    }

    @Test
    public final void shouldExecutePhaseMethod() {
        // given
        List<Entity> messages = Lists.newArrayList();
        messages.add(mockMessage(StateMessageType.SUCCESS, "test"));
        EntityList messagesEntityList = mockEntityList(messages);
        given(stateChangeEntity.getHasManyField(DESCRIBER.getMessagesFieldName())).willReturn(messagesEntityList);

        // when
        stateChangeService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity).setField(TOUCHED_FIELD, true);
    }

    @Test
    public final void shouldNotExecutePhaseMethod() {
        // given
        List<Entity> messages = Lists.newArrayList();
        messages.add(mockMessage(StateMessageType.FAILURE, "test"));
        EntityList messagesEntityList = mockEntityList(messages);
        given(stateChangeEntity.getHasManyField(DESCRIBER.getMessagesFieldName())).willReturn(messagesEntityList);

        // when
        stateChangeService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity, never()).setField(TOUCHED_FIELD, true);
    }

    @Test
    public final void shouldThrowExceptionIfEntityDoesNotHaveMessagesField() {
        // given
        given(stateChangeEntity.getHasManyField(DESCRIBER.getMessagesFieldName())).willReturn(null);
        boolean exceptionWasThrown = false;

        // when
        try {
            stateChangeService.changeState(stateChangeContext);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }

        // then
        assertTrue(exceptionWasThrown);
    }

    @Test
    public final void shouldNotExecutePhaseMethodIfValidationErrorOccured() {
        // given
        List<Entity> messages = Lists.newArrayList();
        Entity validationErrorMessage = mockMessage(StateMessageType.VALIDATION_ERROR, "test");
        messages.add(validationErrorMessage);
        EntityList messagesEntityList = mockEntityList(messages);
        given(stateChangeEntity.getHasManyField(DESCRIBER.getMessagesFieldName())).willReturn(messagesEntityList);

        // when
        stateChangeService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity, never()).setField(TOUCHED_FIELD, true);
        verify(stateChangeEntity, never()).setField(Mockito.eq(TOUCHED_PHASE), Mockito.anyInt());
    }

}
