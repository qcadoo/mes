package com.qcadoo.mes.states.aop;

import static com.qcadoo.mes.states.constants.StateChangeStatus.IN_PROGRESS;
import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;
import static com.qcadoo.mes.states.messages.constants.MessageType.FAILURE;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.annotation.StateChangePhase;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.AnotherChangeInProgressException;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.mes.states.service.StateChangePhaseUtil;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;

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
    private MessageService messageService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private SecurityService securityService;

    /**
     * @return name of field representing entity's state
     */
    protected abstract String getStateFieldName();

    protected MessageService getMessageService() {
        return messageService;
    }

    @Override
    public Entity createNewStateChangeEntity(final Entity owner, final String targetStateString) {
        final Entity persistedOwner = owner.getDataDefinition().save(owner);

        checkForUnfinishedStateChange(persistedOwner);

        final StateChangeEntityDescriber describer = getChangeEntityDescriber();
        final DataDefinition stateChangeDataDefinition = describer.getDataDefinition();
        final StateEnum sourceState = getStateEnum(owner, getStateFieldName());
        final StateEnum targetState = describer.parseStateEnum(targetStateString);
        final Entity stateChangeEntity = stateChangeDataDefinition.create();

        onCreate(stateChangeEntity, persistedOwner, sourceState, targetState);

        return stateChangeDataDefinition.save(stateChangeEntity);
    }

    protected void onCreate(final Entity stateChangeEntity, final Entity owner, final StateEnum sourceState,
            final StateEnum targetState) {
        final StateChangeEntityDescriber describer = getChangeEntityDescriber();
        final Entity shift = shiftsService.getShiftFromDate(new Date());

        stateChangeEntity.setField(describer.getOwnerFieldName(), owner);
        stateChangeEntity.setField(describer.getPhaseFieldName(), 0);

        stateChangeEntity.setField(describer.getSourceStateFieldName(), sourceState.getStringValue());
        stateChangeEntity.setField(describer.getTargetStateFieldName(), targetState.getStringValue());

        stateChangeEntity.setField(describer.getShiftFieldName(), shift);
        stateChangeEntity.setField(describer.getWorkerFieldName(), securityService.getCurrentUserName());

        stateChangeEntity.setField(describer.getStatusFieldName(), StateChangeStatus.IN_PROGRESS.getStringValue());
    }

    /**
     * Checks if given owner entity have not any unfinished state change request.
     * 
     * @param owner
     *            state change's owner entity
     * @throws AnotherChangeInProgressException
     *             if at least one unfinished state change request for given owner entity is found.
     */
    protected final void checkForUnfinishedStateChange(final Entity owner) {
        StateChangeEntityDescriber describer = getChangeEntityDescriber();
        final String ownerFieldName = describer.getOwnerFieldName();
        final String statusFieldName = describer.getStatusFieldName();
        final Set<String> unfinishedStatuses = Sets.newHashSet(IN_PROGRESS.getStringValue(), PAUSED.getStringValue());

        final SearchCriteriaBuilder searchCriteria = describer.getDataDefinition().find();
        searchCriteria.createAlias(ownerFieldName, ownerFieldName);
        searchCriteria.add(SearchRestrictions.eq(ownerFieldName + ".id", owner.getId()));
        searchCriteria.add(SearchRestrictions.in(statusFieldName, unfinishedStatuses));
        if (searchCriteria.list().getTotalNumberOfEntities() > 0) {
            throw new AnotherChangeInProgressException();
        }
    }

    @Override
    public final void addMessage(final Entity stateChangeEntity, final MessageType type, final String translationKey,
            final String... translationArgs) {
        final Entity message = messageService.createMessage(type, translationKey, translationArgs);
        addMessage(stateChangeEntity, message);
    }

    @Override
    public final void addMessage(final Entity stateChangeEntity, final Entity message) {
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
        int phase = 0;
        if (phaseFieldValue instanceof Integer) {
            phase = ((Integer) phaseFieldValue).intValue();
        }
        return phase;
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
        final StateEnum targetState = getStateEnum(stateChangeEntity, describer.getTargetStateFieldName());

        owner.setField(getStateFieldName(), targetState.getStringValue());
        if (owner.getDataDefinition().save(owner).isValid()) {
            stateChangeEntity.setField(describer.getStatusFieldName(), StateChangeStatus.SUCCESSFUL);
            stateChangeEntity.setField(describer.getDateTimeFieldName(), new Date());
        } else {
            stateChangeEntity.setField(describer.getStatusFieldName(), StateChangeStatus.FAILURE);
            addMessage(stateChangeEntity, FAILURE, "states.messages.change.failure.invalidEntity.");
        }

        stateChangeEntity.getDataDefinition().save(stateChangeEntity);
    }

    private StateEnum getStateEnum(final Entity entity, final String fieldName) {
        return getChangeEntityDescriber().parseStateEnum(entity.getStringField(fieldName));
    }

}
