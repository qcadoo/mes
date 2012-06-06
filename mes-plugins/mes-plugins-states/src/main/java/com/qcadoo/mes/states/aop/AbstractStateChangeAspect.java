package com.qcadoo.mes.states.aop;

import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.annotation.StateChangePhase;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

/**
 * Abstract service for changing entity state which provides default implementation.
 */
@Aspect
@Configurable
@DeclarePrecedence("StateChangePhaseAspect, RunInPhaseAspect")
public abstract class AbstractStateChangeAspect implements StateChangeService {

    @Autowired
    protected MessageService messageService;

    /**
     * @return name of field representing entity state
     */
    protected abstract String getStateFieldName();

    public abstract StateChangeEntityDescriber getChangeEntityDescriber();

    @Override
    public Entity createNewStateChangeEntity(final Entity owner, final String targetStateString) {
        final StateChangeEntityDescriber describer = getChangeEntityDescriber();
        final DataDefinition stateChangeDataDefinition = describer.getDataDefinition();

        final Entity stateChangeEntity = stateChangeDataDefinition.create();
        stateChangeEntity.setField(describer.getOwnerFieldName(), owner);
        stateChangeEntity.setField(describer.getPhaseFieldName(), 0);

        final Object sourceState = describer.parseStateEnum(owner.getStringField(getStateFieldName()));
        final Object targetState = describer.parseStateEnum(targetStateString);
        stateChangeEntity.setField(describer.getSourceStateFieldName(), sourceState);
        stateChangeEntity.setField(describer.getTargetStateFieldName(), targetState);

        return stateChangeDataDefinition.save(stateChangeEntity);
    }

    @Override
    public void addMessage(final Entity stateChangeEntity, final MessageType type, final String translationKey,
            final String... translationArgs) {
        final Entity message = messageService.createMessage(type, translationKey, translationArgs);
        addMessage(stateChangeEntity, message);
    }

    @Override
    public void addMessage(final Entity stateChangeEntity, final Entity message) {
        final String messagesFieldName = getChangeEntityDescriber().getMessagesFieldName();
        final List<Entity> messages = stateChangeEntity.getHasManyField(messagesFieldName);
        messages.add(message);
        stateChangeEntity.setField(messagesFieldName, messages);
        getChangeEntityDescriber().getDataDefinition().save(stateChangeEntity);
    }

    @Override
    @Transactional
    public void changeState(final Entity stateChangeEntity) {
        for (int phase = getPhaseValue(stateChangeEntity) + 1; phase <= 50; phase++) {
            changeStatePhase(stateChangeEntity, phase);
        }
        getChangeEntityDescriber().getDataDefinition().save(stateChangeEntity);
        performChangeEntityState(stateChangeEntity);
    }

    private int getPhaseValue(final Entity stateChangeEntity) {
        final Object phaseFieldValue = stateChangeEntity.getField(getChangeEntityDescriber().getPhaseFieldName());
        return phaseFieldValue == null ? 0 : ((Integer) phaseFieldValue).intValue();
    }

    @StateChangePhase
    public void changeStatePhase(final Entity stateChangeEntity, final int currentPhase) {
        stateChangeEntity.setField(getChangeEntityDescriber().getPhaseFieldName(), currentPhase);
    }

    @StateChangePhase
    @Transactional
    protected void performChangeEntityState(final Entity stateChangeEntity) {
        final StateChangeEntityDescriber describer = getChangeEntityDescriber();
        final Entity owner = stateChangeEntity.getBelongsToField(describer.getOwnerFieldName());

        final String targetStateString = stateChangeEntity.getStringField(describer.getTargetStateFieldName());
        final Object targetState = describer.parseStateEnum(targetStateString);
        owner.setField(getStateFieldName(), targetState);

        owner.getDataDefinition().save(owner);
        stateChangeEntity.setField(describer.getFinishedFieldName(), true);
        stateChangeEntity.getDataDefinition().save(stateChangeEntity);
    }

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

    // @Pointcut("execution(* *(..)) && @annotation(com.qcadoo.mes.states.annotation.StateChangePhase) && args(stateChangeEntity, phase) && stateChangeServiceSelector()")
    @Pointcut("execution(* *.changeStatePhase(..)) && args(stateChangeEntity, phase) && stateChangeServiceSelector()")
    public void stateChangingPhase(final Entity stateChangeEntity, final int phase) {
    }

    /**
     * Definie stateChanging pointcut's aditional restriction. Usually pointcut expression looks like "this(TypeName)"
     */
    @Pointcut
    public abstract void stateChangeServiceSelector();

}
