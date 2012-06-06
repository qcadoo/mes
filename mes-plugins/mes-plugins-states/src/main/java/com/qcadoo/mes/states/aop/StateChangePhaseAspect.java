package com.qcadoo.mes.states.aop;

import static org.apache.commons.lang.ArrayUtils.add;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareError;

import com.qcadoo.mes.states.service.StateChangePhaseUtil;
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
        if (StateChangePhaseUtil.canRun(stateChangeService, stateChange)) {
            result = pjp.proceed(add(pjp.getArgs(), 0, stateChangeService));
        }
        return result;
    }

}
