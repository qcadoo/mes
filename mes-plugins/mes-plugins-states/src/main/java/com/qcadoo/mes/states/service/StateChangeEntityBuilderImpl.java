package com.qcadoo.mes.states.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;

@Service
public final class StateChangeEntityBuilderImpl implements StateChangeEntityBuilder {

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private SecurityService securityService;

    @Override
    public Entity buildInitial(final StateChangeEntityDescriber describer, final Entity owner, final StateEnum initialState) {
        final Entity stateChangeEntity = internalBuild(describer, initialState);
        stateChangeEntity.setField(describer.getStatusFieldName(), StateChangeStatus.SUCCESSFUL.getStringValue());
        stateChangeEntity.setField(describer.getDateTimeFieldName(), new Date());

        owner.setField(describer.getOwnerStateFieldName(), initialState.getStringValue());
        owner.setField(describer.getOwnerStateChangesFieldName(), Lists.newArrayList(stateChangeEntity));

        return stateChangeEntity;
    }

    @Override
    public Entity build(final StateChangeEntityDescriber describer, final Entity owner, final StateEnum targetState) {
        final Entity stateChangeEntity = internalBuild(describer, targetState);

        stateChangeEntity.setField(describer.getOwnerFieldName(), owner);
        stateChangeEntity.setField(describer.getSourceStateFieldName(), owner.getStringField(describer.getOwnerStateFieldName()));
        stateChangeEntity.setField(describer.getStatusFieldName(), StateChangeStatus.IN_PROGRESS.getStringValue());

        return stateChangeEntity;
    }

    private Entity internalBuild(final StateChangeEntityDescriber describer, final StateEnum initialState) {
        final Entity stateChangeEntity = describer.getDataDefinition().create();
        final Entity shift = shiftsService.getShiftFromDateWithTime(new Date());

        stateChangeEntity.setField(describer.getTargetStateFieldName(), initialState.getStringValue());

        stateChangeEntity.setField(describer.getShiftFieldName(), shift);
        stateChangeEntity.setField(describer.getWorkerFieldName(), securityService.getCurrentUserName());

        stateChangeEntity.setField(describer.getPhaseFieldName(), 0);

        return stateChangeEntity;
    }

}
