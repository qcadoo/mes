package com.qcadoo.mes.newstates;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentMessagesHolder;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

@Service
public class StateExecutorService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private SecurityService securityService;

    private ComponentMessagesHolder componentMessagesHolder;

    private static final Logger LOGGER = Logger.getLogger(StateExecutorService.class);

    @Transactional
    public <M extends StateService> void changeState(Class<M> serviceMarker, final ViewDefinitionState view, String[] args) {
        componentMessagesHolder = view;
        Optional<GridComponent> maybeGridComponent = view.tryFindComponentByReference("grid");
        if (maybeGridComponent.isPresent()) {
            maybeGridComponent.get().getSelectedEntities().forEach(entity -> {
                // FIXME encja pobierana 2 razy??
                entity = entity.getDataDefinition().getMasterModelEntity(entity.getId());
                changeState(serviceMarker, entity, args[0]);
            });

        } else {
            Optional<FormComponent> maybeForm = view.tryFindComponentByReference("form");
            if (maybeForm.isPresent()) {
                FormComponent formComponent = maybeForm.get();
                Entity entity = formComponent.getPersistedEntityWithIncludedFormValues();
                entity = changeState(serviceMarker, entity, args[0]);
                formComponent.setEntity(entity);
            }
        }
    }

    @Transactional
    public <M extends StateService> Entity changeState(Class<M> serviceMarker, Entity entity, String targetState) {
        try {
            entity = performChangeState(serviceMarker, entity, targetState);
            if (componentMessagesHolder != null) {
                if (entity.isValid()) {
                    componentMessagesHolder.addMessage("states.messages.change.successful", ComponentState.MessageType.SUCCESS);
                    
                } else {
                    componentMessagesHolder.addMessage("states.messages.change.failure", ComponentState.MessageType.FAILURE);
                }
            }

            return entity;
        } catch (Exception exception) {
            LOGGER.warn("Can't perform state change", exception);
//            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
//            stateChangeContext.addMessage("states.messages.change.failure.internalServerError", StateMessageType.FAILURE);
//            stateChangeContext.save();

//        saveStateChangeEntity(describer, sourceState, targetState, entity);
            throw new StateChangeException(exception);
        }

    }

    @Transactional
    private <M extends StateService> Entity performChangeState(Class<M> serviceMarker, Entity entity, String targetState) {
        List<M> services = lookupChangeStateServices(serviceMarker);

        StateChangeEntityDescriber describer = services.stream().findFirst().get().getChangeEntityDescriber();

        String sourceState = entity.getStringField(describer.getSourceStateFieldName());

        if (!canChangeState(describer, entity, targetState)) {
            return entity;
        }

        entity = hookOnValidate(entity, services, sourceState, targetState);
        if (!entity.isValid()) {
            // TODO tu zwracamy błądne encję, czy wyjątek o niepowodzeniu?
            return entity;
        }
        entity = changeState(entity, targetState);
        saveStateChangeEntity(describer, sourceState, targetState, entity);
        
        entity = hookOnBeforeSave(entity, services, sourceState, targetState);
        if (!entity.isValid()) {
            // TODO tu zwracamy błądne encję, czy wyjątek o niepowodzeniu?
            return entity;
        }
        entity = entity.getDataDefinition().save(entity);

        if (!hookOnAfterSave(entity, services, sourceState, targetState)) {
            // TODO tu trzeba wycofać transakcję i cofnąć zmiany na bazie
            // wyjątek? ręcznie?            
        }

        return entity;
    }

    private Entity saveStateChangeEntity(final StateChangeEntityDescriber describer, final String sourceState, final String targetState, Entity owner) {
        Entity buildStateChangeEntity = buildStateChangeEntity(describer, sourceState, targetState, owner);

        // TODO isValid
        return buildStateChangeEntity.getDataDefinition().save(buildStateChangeEntity);
    }

    private Entity buildStateChangeEntity(final StateChangeEntityDescriber describer, final String sourceState, final String targetState, Entity owner) {
        final Entity stateChangeEntity = describer.getDataDefinition().create();
        final Entity shift = shiftsService.getShiftFromDateWithTime(new Date());

        stateChangeEntity.setField(describer.getDateTimeFieldName(), new Date());
        stateChangeEntity.setField(describer.getSourceStateFieldName(), sourceState);
        stateChangeEntity.setField(describer.getTargetStateFieldName(), targetState);
        stateChangeEntity.setField(describer.getShiftFieldName(), shift);
        stateChangeEntity.setField(describer.getWorkerFieldName(), securityService.getCurrentUserName());
        stateChangeEntity.setField(describer.getPhaseFieldName(), 0);
        stateChangeEntity.setField(describer.getOwnerFieldName(), owner);
        stateChangeEntity.setField(describer.getSourceStateFieldName(), owner.getStringField(describer.getOwnerStateFieldName()));
        stateChangeEntity.setField(describer.getStatusFieldName(), StateChangeStatus.SUCCESSFUL.getStringValue());

        return stateChangeEntity;
    }

    private <M extends StateService> boolean canChangeState(StateChangeEntityDescriber describer, Entity owner,
            String targetStateString) {
        final StateEnum sourceState = describer.parseStateEnum(owner.getStringField(describer.getOwnerStateFieldName()));
        final StateEnum targetState = describer.parseStateEnum(targetStateString);
        // TODO wrzucamy błąd do encji?
        if (sourceState != null && !sourceState.canChangeTo(targetState)) {
            return false;
        }
        return true;
    }

    private <M extends StateService> Entity hookOnValidate(Entity entity, Collection<M> services, String sourceState, String targetState) {
        for (StateService service : services) {
            entity = service.onValidate(entity, sourceState, targetState);
        }

        return entity;
    }

    private Entity changeState(Entity entity, String targetState) {
        // TODO zawsze stan będzie w tym polu?
        entity.setField("state", targetState);

        return entity;
    }

    private <M extends StateService> Entity hookOnBeforeSave(Entity entity, Collection<M> services, String sourceState, String targetState) {
        for (StateService service : services) {
            entity = service.onBeforeSave(entity, sourceState, targetState);
        }

        return entity;
    }

    private <M extends StateService> boolean hookOnAfterSave(Entity entity, Collection<M> services, String sourceState, String targetState) {
        for (StateService service : services) {
            entity = service.onAfterSave(entity, sourceState, targetState);
        }

        return entity.isValid();
    }

    private <M extends StateService> List<M> lookupChangeStateServices(Class<M> serviceMarker) {
        Map<String, M> stateServices = applicationContext.getBeansOfType(serviceMarker);

        List<M> services = Lists.newArrayList(stateServices.values());
        AnnotationAwareOrderComparator.sort(services);

        return services;
    }
}
