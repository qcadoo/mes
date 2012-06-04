package com.qcadoo.mes.states.aop;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.hasFailureMessages;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareError;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

@Aspect
public class StateChangePhaseAspect {

    @DeclareError("(execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*(!com.qcadoo.model.api.Entity+,..)) "
            + "|| execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*()))")
    public static final String ERROR = "Only methods with state change entity as a first argument can be annotated using @StateChangePhase";

    @Around("(execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*(..)) "
            + "|| execution(public void com.qcadoo.mes.states.service.StateChangeService.changeState(..))) "
            + "&& args(stateChange,..)")
    public Object omitExecutionIfStateChangeEntityHasErrors(final ProceedingJoinPoint pjp, final Entity stateChange)
            throws Throwable {
        Object result = null;
        List<Entity> messages = stateChange.getHasManyField("messages");
        Preconditions.checkNotNull(messages, "entity " + stateChange + " should have messages has many field!");
        if (!hasFailureMessages(messages)) {
            result = pjp.proceed(pjp.getArgs());
        }
        return result;
    }

}
