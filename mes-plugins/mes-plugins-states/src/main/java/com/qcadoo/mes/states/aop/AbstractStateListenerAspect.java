package com.qcadoo.mes.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;

/**
 * This aspect provides XPI for state change advices (listeners).
 * 
 * @since 1.1.7
 */
@Aspect
public abstract class AbstractStateListenerAspect {

    /**
     * Pointcut for execution state change phase
     * 
     * @param stateChangeEntity
     * @param phase
     */
    @Pointcut("execution(* *.changeStatePhase(..)) && args(stateChangeEntity, phase, ..) && targetServicePointcut()")
    public void phaseExecution(final Entity stateChangeEntity, final int phase) {
    }

    /**
     * Pointcut for changing state join points ({@link StateChangeService#changeState(Entity)}) using additional restrictions from
     * {@link AbstractStateListenerAspect#targetServicePointcut()} pointcut.
     * 
     * @param stateChangeEntity
     *            entity which represent state change flow
     * @param annotation
     */
    @Pointcut("execution(public void com.qcadoo.mes.states.service.StateChangeService+.changeState(..)) "
            + "&& args(stateChangeEntity) && targetServicePointcut()")
    public void changeStateExecution(final Entity stateChangeEntity) {
    }

    @Pointcut("execution(public void com.qcadoo.mes.states.service.client.StateChangeViewClient.changeState(..)) && args(viewContext,..)")
    public void viewClientExecution(final ViewContextHolder viewContext) {
    }

    /**
     * Select {@link StateChangeService} to be woven with this listener. Usually pointcut expression looks like "this(TypeName)"
     */
    @Pointcut
    protected abstract void targetServicePointcut();

    public abstract StateChangeEntityDescriber getStateChangeEntityDescriber();

}
