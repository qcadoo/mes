package com.qcadoo.mes.states.aop;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.joinArgs;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.MockStateChangeDescriber;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class StateChangePhaseAspectTest { // extends StateChangeTest {

    private static final String STATE_FIELD_NAME = "state";

    private static final String TOUCHED_FIELD = "touched";

    private TestStateChangeService stateChangeService;

    private final StateChangeEntityDescriber describer = new MockStateChangeDescriber();

    @Mock
    private Entity stateChangeEntity;

    @Mock
    private DataDefinition stateChangeDD;

    @Aspect
    public static class TestStateChangeService extends AbstractStateChangeAspect {

        @Override
        protected String getStateFieldName() {
            return STATE_FIELD_NAME;
        }

        @Pointcut("this(TestStateChangeService)")
        public void stateChangeServiceSelector() {
        }

        @Override
        public void changeState(final Entity stateChangeEntity) {
            stateChangeEntity.setField(TOUCHED_FIELD, true);
        }

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }
    }

    @Before
    public void init() {
        // super.init();
        MockitoAnnotations.initMocks(this);
        stateChangeService = new TestStateChangeService();
    }

    @Test
    public final void shouldExecutePhaseMethod() {
        // given
        List<Entity> messages = Lists.newArrayList();
        messages.add(mockMessage(MessageType.SUCCESS, "test"));
        EntityList messagesEntityList = mockEntityList(messages);
        given(stateChangeEntity.getHasManyField(describer.getMessagesFieldName())).willReturn(messagesEntityList);

        // when
        stateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity).setField(TOUCHED_FIELD, true);
    }

    @Test
    public final void shouldNotExecutePhaseMethod() {
        // given
        List<Entity> messages = Lists.newArrayList();
        messages.add(mockMessage(MessageType.FAILURE, "test"));
        EntityList messagesEntityList = mockEntityList(messages);
        given(stateChangeEntity.getHasManyField(describer.getMessagesFieldName())).willReturn(messagesEntityList);

        // when
        stateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity, never()).setField(TOUCHED_FIELD, true);
    }

    @Test
    public final void shouldThrowExceptionIfEntityDoesNotHaveMessagesField() {
        // given
        given(stateChangeEntity.getHasManyField(describer.getMessagesFieldName())).willReturn(null);
        boolean exceptionWasThrown = false;

        // when
        try {
            stateChangeService.changeState(stateChangeEntity);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }

        // then
        assertTrue(exceptionWasThrown);
    }

    protected void stubStateChangeEntity(final StateChangeEntityDescriber describer) {
        given(stateChangeEntity.getDataDefinition()).willReturn(stateChangeDD);
        final EntityList emptyEntityList = mockEntityList(Collections.<Entity> emptyList());
        given(stateChangeEntity.getHasManyField(describer.getMessagesFieldName())).willReturn(emptyEntityList);
    }

    protected EntityList mockEntityList(final List<Entity> entities) {
        EntityList entityList = mock(EntityList.class);
        given(entityList.iterator()).willReturn(entities.iterator());
        given(entityList.isEmpty()).willReturn(entities.isEmpty());
        return entityList;
    }

    protected Entity mockMessage(final MessageType type, final String translationKey, final String... translationArgs) {
        Entity message = mock(Entity.class);
        mockEntityField(message, MessageFields.TYPE, type);
        mockEntityField(message, MessageFields.TRANSLATION_KEY, translationKey);
        mockEntityField(message, MessageFields.TRANSLATION_ARGS, joinArgs(translationArgs));
        return message;
    }

    protected void mockEntityField(final Entity entity, final String fieldName, final Object fieldValue) {
        given(entity.getField(fieldName)).willReturn(fieldValue);
        given(entity.getStringField(fieldName)).willReturn(fieldValue == null ? null : fieldValue.toString());
    }

}
