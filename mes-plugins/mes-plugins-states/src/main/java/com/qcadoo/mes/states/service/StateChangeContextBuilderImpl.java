package com.qcadoo.mes.states.service;

import static com.qcadoo.mes.states.constants.StateChangeStatus.IN_PROGRESS;
import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeContextImpl;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.AnotherChangeInProgressException;
import com.qcadoo.mes.states.exception.StateTransitionNotAlloweException;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;

@Service
public final class StateChangeContextBuilderImpl implements StateChangeContextBuilder {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private SecurityService securityService;

    @Override
    @Transactional
    public StateChangeContext build(final StateChangeEntityDescriber describer, final Entity owner, final String targetStateString) {
        final Entity persistedOwner = owner.getDataDefinition().save(owner);
        final DataDefinition stateChangeDataDefinition = describer.getDataDefinition();
        final StateEnum sourceState = describer.parseStateEnum(owner.getStringField(describer.getOwnerStateFieldName()));
        final StateEnum targetState = describer.parseStateEnum(targetStateString);
        if (sourceState != null && !sourceState.canChangeTo(targetState)) {
            throw new StateTransitionNotAlloweException(sourceState, targetState);
        }
        final Entity stateChangeEntity = stateChangeDataDefinition.create();

        onCreate(describer, stateChangeEntity, persistedOwner, sourceState, targetState);

        checkForUnfinishedStateChange(describer, persistedOwner);
        return new StateChangeContextImpl(stateChangeDataDefinition.save(stateChangeEntity), describer, messageService);
    }

    @Override
    @Transactional
    public StateChangeContext build(final StateChangeEntityDescriber describer, final Entity stateChangeEntity) {
        return new StateChangeContextImpl(stateChangeEntity, describer, messageService);
    }

    protected void onCreate(final StateChangeEntityDescriber describer, final Entity stateChangeEntity, final Entity owner,
            final StateEnum sourceState, final StateEnum targetState) {
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
    protected final void checkForUnfinishedStateChange(final StateChangeEntityDescriber describer, final Entity owner) {
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
}
