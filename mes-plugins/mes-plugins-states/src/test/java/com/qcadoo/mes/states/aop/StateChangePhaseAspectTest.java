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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.MockStateChangeDescriber;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateChangeTest;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class StateChangePhaseAspectTest extends StateChangeTest {

    private static final String STATE_FIELD_NAME = "state";

    private static final String TOUCHED_FIELD = "touched";

    private static final String TOUCHED_PHASE = "touchedPhase";

    private TestStateChangeService stateChangeService;

    @Aspect
    public static class TestStateChangeService extends AbstractStateChangeAspect {

        @Override
        protected String getStateFieldName() {
            return STATE_FIELD_NAME;
        }

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
