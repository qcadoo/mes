package com.qcadoo.mes.states.aop;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.MockStateChangeDescriber;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateChangeTest;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class StateChangePhaseAspectTest extends StateChangeTest {

    private static final String STATE_FIELD_NAME = "state";

    private static final String TOUCHED_FIELD = "touched";

    private TestStateChangeService stateChangeService;

    private final StateChangeEntityDescriber describer = new MockStateChangeDescriber();

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

        @Override
        protected void changeStatePhase(Entity stateChangeEntity, Integer phaseNumber) {
            // TODO Auto-generated method stub

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

}
