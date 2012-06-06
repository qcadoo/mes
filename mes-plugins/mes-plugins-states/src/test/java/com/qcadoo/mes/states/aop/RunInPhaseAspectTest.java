package com.qcadoo.mes.states.aop;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.states.MockStateChangeDescriber;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateChangeTest;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class RunInPhaseAspectTest extends StateChangeTest {

    private static final int FIRST_PHASE = 1;

    private static final int TEST_PHASE = 3;

    private static final int LAST_PHASE = AbstractStateChangeAspect.DEFAULT_NUM_OF_PHASES;

    private TestStateChangeAspect stateChangeService;

    @Aspect
    public final static class TestStateChangeAspect extends AbstractStateChangeAspect {

        @Override
        protected String getStateFieldName() {
            return "state";
        }

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

        @Pointcut("this(TestStateChangeAspect)")
        public void stateChangeServiceSelector() {
        }

        @Override
        protected void performChangeEntityState(final Entity stateChangeEntity) {
            stateChangeEntity.setField("finished", true);
        }

        @Override
        protected void changeStatePhase(final Entity stateChangeEntity, final Integer phaseNumber) {
        }

    }

    @Aspect
    public static class TestListener {

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.Before("TestStateChangeAspect.stateChangingPhase(stateEntity, phase)")
        public void testAdvice(Entity stateEntity, final int phase) {
            stateEntity.setField("onceChecked", true);
            stateEntity.setField("checkedPhase", phase);
        }

        @RunInPhase({ FIRST_PHASE, TEST_PHASE, LAST_PHASE })
        @org.aspectj.lang.annotation.Before("TestStateChangeAspect.stateChangingPhase(stateEntity, phase)")
        public void testAdvice2(final Entity stateEntity, final int phase) {
            stateEntity.setField("multiChecked", true);
            stateEntity.setField("anotherCheckedPhase", phase);
        }

    }

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        stateChangeService = new TestStateChangeAspect();
        final EntityList emptyEntityList = mockEntityList(Collections.<Entity> emptyList());
        given(stateChangeEntity.getHasManyField(describer.getMessagesFieldName())).willReturn(emptyEntityList);
    }

    @Test
    public final void shouldFireListener() {
        // when
        stateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity).setField("onceChecked", true);
        verify(stateChangeEntity).setField(Mockito.eq("checkedPhase"), Mockito.anyInt());
        verify(stateChangeEntity).setField("checkedPhase", TEST_PHASE);

        verify(stateChangeEntity, times(3)).setField("multiChecked", true);
        verify(stateChangeEntity, times(3)).setField(Mockito.eq("anotherCheckedPhase"), Mockito.anyInt());
        verify(stateChangeEntity).setField("anotherCheckedPhase", FIRST_PHASE);
        verify(stateChangeEntity).setField("anotherCheckedPhase", TEST_PHASE);
        verify(stateChangeEntity).setField("anotherCheckedPhase", LAST_PHASE);
        verify(stateChangeEntity).setField("finished", true);
    }

}
