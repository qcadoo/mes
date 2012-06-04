package com.qcadoo.mes.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

/**
 * Service for changing entity state. Methods {@link AbstractStateChangeAspect#getModelName()},
 * {@link AbstractStateChangeAspect#getPluginIdentifier()}, {@link AbstractStateChangeAspect#getStateFieldName()} determines
 * entity's field which should be prevented for changing outside {@link StateChangeService}.
 */
@Aspect
public abstract class AbstractStateChangeAspect implements StateChangeService {

    /**
     * @return name of field representing entity state
     */
    protected abstract String getStateFieldName();

    /**
     * @return entity plugin identifier
     */
    protected abstract String getPluginIdentifier();

    /**
     * @return entity model name
     */
    protected abstract String getModelName();

    /**
     * Determine pointcut for changing state join points ({@link StateChangeService#changeState(Entity)}) using additional
     * restrictions from {@link AbstractStateChangeAspect#stateChangeServiceSelector()} pointcut.
     * 
     * @param stateChangeEntity
     *            entity which represent state change flow
     * @param annotation
     */
    @Pointcut("execution(public void com.qcadoo.mes.states.service.StateChangeService.changeState(..)) "
            + "&& args(stateChangeEntity,..) && stateChangeServiceSelector()")
    public void stateChanging(final Entity stateChangeEntity) {
    }

    /**
     * Definie stateChanging pointcut's aditional restriction. Usually pointcut expression looks like "this(TypeName)"
     */
    @Pointcut
    public abstract void stateChangeServiceSelector();

    /**
     * This advice prevent from setting entity state field outside dedicated StateChangeService.
     * 
     * @param fieldName
     * @param entity
     * @throws Throwable
     */
    @Before("call(public void com.qcadoo.model.api.Entity.setField(..)) && args(fieldName, *) "
            + "&& !cflow(stateChangeServiceSelector()) && target(entity)")
    public void throwExceptionIfStateIsChangedOutsideSCS(final String fieldName, final Entity entity) throws Throwable {
        if (entityMatchModel(entity, fieldName)) {
            throw new IllegalArgumentException(
                    "Changing entity state outside StateChangeService or their subclasses is not permitted.");
        }
    }

    /**
     * @param entity
     *            to be checked
     * @return true if entity plugin identifier & model name match values returned by
     *         {@link AbstractStateChangeAspect#getPluginIdentifier()} and {@link AbstractStateChangeAspect}
     *         {@link #getModelName()}
     */
    private boolean entityMatchModel(final Entity entity, final String fieldName) {
        if (!getStateFieldName().equals(fieldName)) {
            return false;
        }
        DataDefinition dataDefinition = entity.getDataDefinition();
        if (dataDefinition == null) {
            return false;
        }
        return dataDefinition.getName().equals(getModelName())
                && dataDefinition.getPluginIdentifier().equals(getPluginIdentifier());
    }

}
