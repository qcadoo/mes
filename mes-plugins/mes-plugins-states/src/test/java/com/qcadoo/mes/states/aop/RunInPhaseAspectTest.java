package com.qcadoo.mes.states.aop;

import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class RunInPhaseAspectTest extends StateChangeTest {

    private static final int FIRST_PHASE = 1;

    private static final int TEST_PHASE = 3;

    private static final int LAST_PHASE = 8;

    private StateChangeService stateChangeService;

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

        @Override
        protected void performChangeEntityState(final Entity stateChangeEntity) {
            stateChangeEntity.setField("finished", true);
        }

        @Override
        protected void changeStatePhase(final Entity stateChangeEntity, final Integer phaseNumber) {
            stateChangeEntity.setField("executedPhase", phaseNumber);
        }

        @Override
        protected int getNumOfPhases() {
            return LAST_PHASE;
        }

    }

    @Aspect
    public final static class AnotherStateChangeAspect extends AbstractStateChangeAspect {

        @Override
        protected String getStateFieldName() {
            return "state";
        }

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

        @Override
        protected void changeStatePhase(final Entity stateChangeEntity, final Integer phaseNumber) {
        }

        @Override
        protected int getNumOfPhases() {
            return LAST_PHASE;
        }

        @Override
        protected void performChangeEntityState(final Entity stateChangeEntity) {
        }

    }

    @Aspect
    public final static class BreakStateChangeAspect extends AbstractStateChangeAspect {

        @Override
        protected String getStateFieldName() {
            return "state";
        }

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

        @Override
        protected void changeStatePhase(final Entity stateChangeEntity, final Integer phaseNumber) {
        }

        @Override
        protected int getNumOfPhases() {
            return LAST_PHASE;
        }

        @Override
        protected void performChangeEntityState(final Entity stateChangeEntity) {
        }

    }

    @Aspect
    public static class TestListener extends AbstractStateListenerAspect {

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.Before("phaseExecution(stateEntity, phase)")
        public void testAdvice(final Entity stateEntity, final int phase) {
            stateEntity.setField("onceChecked", true);
            stateEntity.setField("checkedPhase", phase);
        }

        @RunInPhase({ FIRST_PHASE, TEST_PHASE, LAST_PHASE })
        @org.aspectj.lang.annotation.Before("phaseExecution(stateEntity, phase)")
        public void testAdvice2(final Entity stateEntity, final int phase) {
            stateEntity.setField("multiChecked", true);
            stateEntity.setField("anotherCheckedPhase", phase);
        }

        @org.aspectj.lang.annotation.Before("phaseExecution(stateEntity, phase)")
        public void testAdvice3(final Entity stateEntity, final int phase) {
            stateEntity.setField("withoutAtPhase", true);
        }

        @Pointcut("this(TestStateChangeAspect)")
        protected void targetServicePointcut() {
        }

        @Override
        public StateChangeEntityDescriber getStateChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

    }

    @Aspect
    public static class AnotherListener extends AbstractStateListenerAspect {

        @org.aspectj.lang.annotation.Before("phaseExecution(stateEntity, phase)")
        public void testAdvice(final Entity stateEntity, final int phase) {
            stateEntity.setField("yetAnotherOnceChecked", true);
            stateEntity.setField("yetAnotherCheckedPhase", phase);
        }

        @Pointcut("this(AnotherStateChangeAspect)")
        protected void targetServicePointcut() {
        }

        @Override
        public StateChangeEntityDescriber getStateChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }
    }

    @Aspect
    @RunInPhase(FIRST_PHASE)
    public static class ClassLevelAnnotatedListener extends AbstractStateListenerAspect {

        @org.aspectj.lang.annotation.Before("phaseExecution(stateEntity, phase)")
        public void classAnnotationAdvice(final Entity stateEntity, final int phase) {
            stateEntity.setField("classAnnotationCheckedPhase", phase);
        }

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.Before("phaseExecution(stateEntity, phase)")
        public void methodAnnotationAdvice(final Entity stateEntity, final int phase) {
            stateEntity.setField("methodAnnotationCheckedPhase", phase);
        }

        @Pointcut("this(TestStateChangeAspect)")
        protected void targetServicePointcut() {
        }

        @Override
        public StateChangeEntityDescriber getStateChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

    }

    @Aspect
    public static class BreakingListener extends AbstractStateListenerAspect {

        private static final StateChangeEntityDescriber DESCRIBER = new MockStateChangeDescriber();

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.Before("phaseExecution(stateEntity, phase)")
        public void testAdviceBeforeTest(final Entity stateEntity, final int phase) {
            stateEntity.setField("beforeTest", true);
        }

        @RunInPhase(LAST_PHASE)
        @org.aspectj.lang.annotation.Before("phaseExecution(stateEntity, phase)")
        public void testAdviceBeforeLast(final Entity stateEntity, final int phase) {
            stateEntity.setField("beforeLast", true);
        }

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.After("phaseExecution(stateEntity, phase)")
        public void testAdviceAfterTest(final Entity stateEntity, final int phase) {
            stateEntity.setField("afterTest", true);
            stateEntity.setField(DESCRIBER.getStatusFieldName(), PAUSED.getStringValue());
            given(stateEntity.getField(DESCRIBER.getStatusFieldName())).willReturn(PAUSED.getStringValue());
            given(stateEntity.getStringField(DESCRIBER.getStatusFieldName())).willReturn(PAUSED.getStringValue());
        }

        @Pointcut("this(BreakStateChangeAspect)")
        protected void targetServicePointcut() {
        }

        @Override
        public StateChangeEntityDescriber getStateChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }
    }

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        stateChangeService = new TestStateChangeAspect();
        final EntityList emptyEntityList = mockEntityList(Collections.<Entity> emptyList());
        stubStateChangeEntity(DESCRIBER);
        given(stateChangeEntity.getHasManyField(DESCRIBER.getMessagesFieldName())).willReturn(emptyEntityList);
    }

    @Test
    public final void shouldFireListeners() {
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

        verify(stateChangeEntity, times(LAST_PHASE)).setField("withoutAtPhase", true);

        verify(stateChangeEntity, never()).setField("yetAnotherOnceChecked", true);
        verify(stateChangeEntity, never()).setField(Mockito.eq("yetAnotherCheckedPhase"), Mockito.anyInt());

        verify(stateChangeEntity).setField("finished", true);
    }

    @Test
    public final void shouldContinueFromLastPhaseChain() {
        // given
        given(stateChangeEntity.getField(DESCRIBER.getPhaseFieldName())).willReturn(TEST_PHASE);

        // when
        stateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity, times(1)).setField("multiChecked", true);
        verify(stateChangeEntity, times(1)).setField(Mockito.eq("anotherCheckedPhase"), Mockito.anyInt());
        verify(stateChangeEntity, never()).setField("anotherCheckedPhase", FIRST_PHASE);
        verify(stateChangeEntity, never()).setField("anotherCheckedPhase", TEST_PHASE);
        verify(stateChangeEntity).setField("anotherCheckedPhase", LAST_PHASE);

        verify(stateChangeEntity, times(LAST_PHASE - TEST_PHASE)).setField(Mockito.eq("executedPhase"), Mockito.anyInt());

        verify(stateChangeEntity).setField("finished", true);
    }

    @Test
    public final void shouldBreakPhaseChainExecution() {
        // given
        stateChangeService = new BreakStateChangeAspect();

        // when
        stateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity).setField("beforeTest", true);
        verify(stateChangeEntity).setField("afterTest", true);
        verify(stateChangeEntity, never()).setField("beforeLast", true);
    }

    @Test
    public final void shouldCheckClassLevelAnnotationIfMethodLevelAnnotationIsNotPresent() {
        // when
        stateChangeService.changeState(stateChangeEntity);

        // then
        verify(stateChangeEntity).setField("classAnnotationCheckedPhase", FIRST_PHASE);
        verify(stateChangeEntity).setField("methodAnnotationCheckedPhase", TEST_PHASE);

    }
}
