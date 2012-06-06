package com.qcadoo.mes.states.aop;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Before;
import org.junit.Ignore;
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

    private static final int TEST_PHASE = 3;

    private TestStateChangeAspect stateChangeService;

    @Aspect
    public static class TestStateChangeAspect extends AbstractStateChangeAspect {

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

    }

    @Aspect
    public static class TestListener {

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.Before("TestStateChangeAspect.stateChangingPhase(stateEntity, phase)")
        public void testAdvice(final Entity stateEntity, final int phase) {
            stateEntity.setField("onceChecked", true);
            stateEntity.setField("checkedPhase", phase);
        }

        @RunInPhase({ TEST_PHASE, 8 })
        @org.aspectj.lang.annotation.Before("TestStateChangeAspect.stateChangingPhase(stateEntity, phase)")
        public void testAdvice2(final Entity stateEntity, final int phase) {
            stateEntity.setField("multiChecked", true);
        }

    }

    @Before
    public final void init() {
        // super.init();
        MockitoAnnotations.initMocks(this);
        stateChangeService = new TestStateChangeAspect();
        final EntityList emptyEntityList = mockEntityList(Collections.<Entity> emptyList());
        given(stateChangeEntity.getHasManyField(describer.getMessagesFieldName())).willReturn(emptyEntityList);
    }

    @Test
    @Ignore
    public final void shouldFireListener() {
        // when
        stateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity).setField("onceChecked", true);
        verify(stateChangeEntity, times(2)).setField("multiChecked", true);
        verify(stateChangeEntity).setField(Mockito.eq("checkedPhase"), Mockito.anyInt());
        verify(stateChangeEntity).setField("checkedPhase", TEST_PHASE);
        verify(stateChangeEntity).setField("finished", true);
    }

}
