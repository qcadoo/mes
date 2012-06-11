package com.qcadoo.mes.states.aop;

import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.annotation.StateChangePhase;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.mes.states.service.StateChangePhaseUtil;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

/**
 * Abstract service for changing entity state which provides default implementation.
 * 
 * @since 1.1.7
 */
@Aspect
@Configurable
@DeclarePrecedence("com.qcadoo.mes.states.aop.StateChangePhaseAspect, com.qcadoo.mes.states.aop.RunInPhaseAspect")
public abstract class AbstractStateChangeAspect implements StateChangeService {

    protected static final int DEFAULT_NUM_OF_PHASES = 1;

    @Autowired
    protected MessageService messageService;

    /**
     * @return name of field representing entity's state
     */
    protected abstract String getStateFieldName();

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
        final StateChangeEntityDescriber describer = getChangeEntityDescriber();
        describer.checkFields();
        for (int phase = getPhaseValue(stateChangeEntity) + 1; phase <= getNumOfPhases(); phase++) {
            if (StateChangePhaseUtil.canRun(this, stateChangeEntity)) {
                stateChangeEntity.setField(describer.getPhaseFieldName(), phase);
                changeStatePhase(stateChangeEntity, phase);
            }
        }
        describer.getDataDefinition().save(stateChangeEntity);
        performChangeEntityState(stateChangeEntity);
    }

    /**
     * Get number of state change phases. Default is 1.
     * 
     * @return number of phases
     */
    protected int getNumOfPhases() {
        return DEFAULT_NUM_OF_PHASES;
    }

    private int getPhaseValue(final Entity stateChangeEntity) {
        final Object phaseFieldValue = stateChangeEntity.getField(getChangeEntityDescriber().getPhaseFieldName());
        return phaseFieldValue == null ? 0 : ((Integer) phaseFieldValue).intValue();
    }

    /**
     * Single state change phase join point.
     * 
     * @param stateChangeEntity
     * @param phaseNumber
     */
    protected abstract void changeStatePhase(final Entity stateChangeEntity, final Integer phaseNumber);

    @StateChangePhase
    @Transactional
    protected void performChangeEntityState(final Entity stateChangeEntity) {
        final StateChangeEntityDescriber describer = getChangeEntityDescriber();
        final Entity owner = stateChangeEntity.getBelongsToField(describer.getOwnerFieldName());

        final String targetStateString = stateChangeEntity.getStringField(describer.getTargetStateFieldName());
        final Object targetState = describer.parseStateEnum(targetStateString);
        owner.setField(getStateFieldName(), targetState);

        owner.getDataDefinition().save(owner);
        stateChangeEntity.setField(describer.getStatusFieldName(), true);
        stateChangeEntity.getDataDefinition().save(stateChangeEntity);
    }

}
