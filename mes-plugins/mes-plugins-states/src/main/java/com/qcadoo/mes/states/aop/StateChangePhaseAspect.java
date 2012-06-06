package com.qcadoo.mes.states.aop;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.hasFailureMessages;
import static org.apache.commons.lang.ArrayUtils.add;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareError;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.Entity;

@Aspect
public class StateChangePhaseAspect {

    @DeclareError("(execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*(!com.qcadoo.model.api.Entity+,..)) "
            + "|| execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*()))")
    public static final String ERROR = "Only methods with state change entity as a first argument can be annotated using @StateChangePhase";

    @Around("(execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*(..)) "
            + "|| execution(public void com.qcadoo.mes.states.service.StateChangeService.changeState(..))) "
            + "&& args(stateChange,..) && this(stateChangeService)")
    public Object omitExecutionIfStateChangeEntityHasErrors(final ProceedingJoinPoint pjp, final Entity stateChange,
            final StateChangeService stateChangeService) throws Throwable {
        Object result = null;
        final StateChangeEntityDescriber describer = stateChangeService.getChangeEntityDescriber();

        boolean isFinished = stateChange.getBooleanField(describer.getFinishedFieldName());
        List<Entity> messages = stateChange.getHasManyField(describer.getMessagesFieldName());

        Preconditions.checkNotNull(messages, "entity " + stateChange + " should have messages has many field!");
        if (!isFinished && !hasFailureMessages(messages)) {
            result = pjp.proceed(add(pjp.getArgs(), 0, stateChangeService));
        }
        return result;
    }

}
