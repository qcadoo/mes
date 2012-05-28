package com.qcadoo.mes.states.service;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.internal.DefaultEntity;

public class AbstractStateChangeServiceTest {

    private static final String STATE_FIELD_NAME = "state";

    private static final String STATE_FIELD_VALUE = "someState";

    private static final String PLUGIN_IDENTIFIER = "pluginIdentifier";

    private static final String MODEL_NAME = "modelName";

    private static final String TARGET_ENTITY_FIELD = "targetEntity";

    @Aspect
    public static class TestStateChangeAspect {

        @org.aspectj.lang.annotation.Before("TestStateChangeService.stateChanging(stateChange)")
        public void markEntityBefore(final Entity stateChange) {
            stateChange.setField("marked", true);
        }

    }

    @Aspect
    public static class TestStateChangeService extends AbstractStateChangeService {

        @Override
        protected String getStateFieldName() {
            return STATE_FIELD_NAME;
        }

        @Override
        protected String getPluginIdentifier() {
            return PLUGIN_IDENTIFIER;
        }

        @Override
        protected String getModelName() {
            return MODEL_NAME;
        }

        @Pointcut("this(TestStateChangeService)")
        public void stateChangeServiceSelector() {
        }

        @Override
        public void changeState(final Entity stateChangeEntity) {
            Entity targetEntity = stateChangeEntity.getBelongsToField(TARGET_ENTITY_FIELD);
            targetEntity.setField(getStateFieldName(), stateChangeEntity.getField("targetState"));
        }

    }

    @Aspect
    public static class AnotherStateChangeService extends AbstractStateChangeService {

        @Override
        protected String getStateFieldName() {
            return STATE_FIELD_NAME;
        }

        @Override
        protected String getPluginIdentifier() {
            return PLUGIN_IDENTIFIER;
        }

        @Override
        protected String getModelName() {
            return MODEL_NAME;
        }

        @Pointcut("this(AnotherStateChangeService)")
        public void stateChangeServiceSelector() {
        }

        @Override
        public void changeState(final Entity stateChangeEntity) {
            Entity targetEntity = stateChangeEntity.getBelongsToField(TARGET_ENTITY_FIELD);
            targetEntity.setField(getStateFieldName(), stateChangeEntity.getField("targetState"));
        }

    }

    private Entity entity;

    @Mock
    private DataDefinition dataDefinition;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        entity = new DefaultEntity(dataDefinition);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void shouldThrowExceptionWhenSetFragileFieldOnGuardedModel() {
        // given
        stubEntityMock(PLUGIN_IDENTIFIER, MODEL_NAME);

        // when
        entity.setField(STATE_FIELD_NAME, STATE_FIELD_VALUE);
    }

    @Test
    public final void shouldSetFragileFieldOnNotGuardedModel() {
        // given
        stubEntityMock("otherPlugin", "otherModel");

        // when
        entity.setField(STATE_FIELD_NAME, STATE_FIELD_VALUE);

        // then
        assertEquals(STATE_FIELD_VALUE, entity.getField(STATE_FIELD_NAME));
    }

    @Test
    public final void shouldSetNotFragileFieldOnGuardedModel() {
        // given
        stubEntityMock(PLUGIN_IDENTIFIER, MODEL_NAME);

        // when
        entity.setField("otherField", 5);

        // then
        assertEquals(5, entity.getField("otherField"));
    }

    @Test
    public final void shouldSetNotFragileFieldOnNotGuardedModel() {
        // given
        stubEntityMock("otherPlugin", "otherModel");

        // when
        entity.setField("otherField", 5);

        // then
        assertEquals(5, entity.getField("otherField"));
    }

    @Test
    public final void shouldFireListenersAndChangeState() {
        // given
        TestStateChangeService testStateChangeService = new TestStateChangeService();
        Entity stateChangeEntity = mock(Entity.class);
        given(stateChangeEntity.getBelongsToField(TARGET_ENTITY_FIELD)).willReturn(entity);
        stubStringField(stateChangeEntity, "targetState", STATE_FIELD_VALUE);
        stubEntityMock("", "");

        // when
        testStateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity).setField("marked", true);
        assertEquals(STATE_FIELD_VALUE, entity.getField(STATE_FIELD_NAME));
    }

    @Test
    public final void shouldNotFireListenersForOtherStateChangeService() {
        // given
        AnotherStateChangeService anotherStateChangeService = new AnotherStateChangeService();
        Entity stateChangeEntity = mock(Entity.class);
        given(stateChangeEntity.getBelongsToField(TARGET_ENTITY_FIELD)).willReturn(entity);
        stubStringField(stateChangeEntity, "targetState", STATE_FIELD_VALUE);
        stubEntityMock("", "");

        // when
        anotherStateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity, never()).setField("marked", true);
        assertEquals(STATE_FIELD_VALUE, entity.getField(STATE_FIELD_NAME));
    }

    private void stubStringField(final Entity entity, final String fieldName, final String fieldValue) {
        given(entity.getStringField(fieldName)).willReturn(fieldValue);
        given(entity.getField(fieldName)).willReturn(fieldValue);
    }

    private void stubEntityMock(final String pluginIdentifier, final String modelName) {
        given(dataDefinition.getPluginIdentifier()).willReturn(pluginIdentifier);
        given(dataDefinition.getName()).willReturn(modelName);

    }

}
