package com.qcadoo.mes.states.aop;

import static com.qcadoo.mes.states.constants.StateChangeStatus.IN_PROGRESS;
import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;
import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;
import static com.qcadoo.mes.states.messages.constants.StateMessageType.FAILURE;

import java.util.Date;
import java.util.Set;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeContextImpl;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.AnotherChangeInProgressException;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
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
    @Transactional
    public StateChangeContext buildStateChangeContext(final Entity owner, final String targetStateString) {
        final Entity persistedOwner = owner.getDataDefinition().save(owner);
        final StateChangeEntityDescriber describer = getChangeEntityDescriber();
        final DataDefinition stateChangeDataDefinition = describer.getDataDefinition();
        final StateEnum sourceState = describer.parseStateEnum(owner.getStringField(getStateFieldName()));
        final StateEnum targetState = describer.parseStateEnum(targetStateString);
        final Entity stateChangeEntity = stateChangeDataDefinition.create();

        onCreate(stateChangeEntity, persistedOwner, sourceState, targetState);

        checkForUnfinishedStateChange(persistedOwner);
        return new StateChangeContextImpl(stateChangeDataDefinition.save(stateChangeEntity), describer, messageService);
    }

    @Override
    @Transactional
    public StateChangeContext buildStateChangeContext(final Entity stateChangeEntity) {
        return new StateChangeContextImpl(stateChangeEntity, getChangeEntityDescriber(), messageService);
    }

    protected void onCreate(final Entity stateChangeEntity, final Entity owner, final StateEnum sourceState,
            final StateEnum targetState) {
        final StateChangeEntityDescriber describer = getChangeEntityDescriber();
        final Entity shift = shiftsService.getShiftFromDateWithTime(new Date());

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
    @Transactional
    public void changeState(final StateChangeContext stateChangeContext) {
        stateChangeContext.save();
        final StateChangeEntityDescriber describer = stateChangeContext.getDescriber();
        try {
            describer.checkFields();
            for (int phase = stateChangeContext.getPhase() + 1; phase <= getNumOfPhases(); phase++) {
                if (StateChangePhaseUtil.canRun(stateChangeContext)) {
                    stateChangeContext.setPhase(phase);
                    changeStatePhase(stateChangeContext, phase);
                }
            }
            final Entity owner = stateChangeContext.getOwner();
            stateChangeContext.setField(describer.getOwnerFieldName(), owner.getDataDefinition().save(owner));
            stateChangeContext.save();
            performChangeEntityState(stateChangeContext);
        } catch (Exception e) {
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            stateChangeContext.addMessage("states.messages.change.failure.internalServerError", StateMessageType.FAILURE);
            stateChangeContext.save();
            throw new StateChangeException(e);
        }
    }

    /**
     * Get number of state change phases. Default is 1.
     * 
     * @return number of phases
     */
    protected int getNumOfPhases() {
        return DEFAULT_NUM_OF_PHASES;
    }

    /**
     * Single state change phase join point.
     * 
     * @param stateChangeEntity
     * @param phaseNumber
     */
    protected abstract void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber);

    @Transactional
    protected void performChangeEntityState(final StateChangeContext stateChangeContext) {
        if (!StateChangePhaseUtil.canRun(stateChangeContext)) {
            return;
        }
        final StateChangeEntityDescriber describer = stateChangeContext.getDescriber();
        final Entity owner = stateChangeContext.getOwner();
        final StateEnum targetState = stateChangeContext.getStateEnumValue(describer.getTargetStateFieldName());

        boolean ownerIsValid = owner.isValid();
        if (ownerIsValid) {
            owner.setField(getStateFieldName(), targetState.getStringValue());
            ownerIsValid = owner.getDataDefinition().save(owner).isValid();
        }

        if (ownerIsValid) {
            markAsSuccessful(stateChangeContext);
        } else {
            markAsInvalid(stateChangeContext);
        }

        stateChangeContext.save();
    }

    private void markAsSuccessful(final StateChangeContext stateChangeContext) {
        final StateChangeEntityDescriber describer = stateChangeContext.getDescriber();
        stateChangeContext.setStatus(SUCCESSFUL);
        stateChangeContext.setField(describer.getDateTimeFieldName(), new Date());
    }

    private void markAsInvalid(final StateChangeContext stateChangeContext) {
        stateChangeContext.setStatus(StateChangeStatus.FAILURE);
        stateChangeContext.addMessage("states.messages.change.failure.invalidEntity.", FAILURE);
    }

}
