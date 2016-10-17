package com.qcadoo.mes.newstates;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.model.api.validators.GlobalMessage;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentMessagesHolder;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    public <M extends StateService> void changeState(Class<M> serviceMarker, final ViewDefinitionState view,
            String[] args) {
        Optional<GridComponent> maybeGridComponent = view.tryFindComponentByReference("grid");
        if (maybeGridComponent.isPresent()) {
            maybeGridComponent.get().getSelectedEntities().forEach(entity -> {
                entity = entity.getDataDefinition().getMasterModelEntity(entity.getId());
                entity = changeState(serviceMarker, entity, args[0]);

                componentMessagesHolder = view;
                copyMessages(entity);
            });

        } else {
            componentMessagesHolder = view;
            Optional<FormComponent> maybeForm = view.tryFindComponentByReference("form");
            if (maybeForm.isPresent()) {
                FormComponent formComponent = maybeForm.get();
                formComponent.performEvent(view, "save", new String[0]);
                Entity entity = formComponent.getEntity().getDataDefinition().get(formComponent.getEntityId());
                if (entity.isValid()) {
                    entity = changeState(serviceMarker, entity, args[0]);
                    formComponent.setEntity(entity);
                }
            }
        }
    }

    public <M extends StateService> Entity changeState(Class<M> serviceMarker,
            Entity entity, String targetState) {
        List<M> services = lookupChangeStateServices(serviceMarker);
        StateChangeEntityDescriber describer = services.stream().findFirst().get().getChangeEntityDescriber();
        String sourceState = entity.getStringField(describer.getOwnerStateFieldName());

        Entity stateChangeEntity = buildStateChangeEntity(describer, entity, sourceState, targetState);

        try {
            entity = performChangeState(services, entity, stateChangeEntity, describer);

            if (entity.isValid()) {
                copyMessages(entity);
                saveStateChangeEntity(stateChangeEntity, StateChangeStatus.SUCCESSFUL);
                message("states.messages.change.successful", ComponentState.MessageType.SUCCESS);
            } else {
                saveStateChangeEntity(stateChangeEntity, StateChangeStatus.FAILURE);
                entity = rollbackStateChange(entity, sourceState);
                message("states.messages.change.failure", ComponentState.MessageType.FAILURE);
            }

        } catch (EntityRuntimeException entityException) {
            copyMessages(entityException.getEntity(), entity);
            entity = rollbackStateChange(entity, sourceState);
            saveStateChangeEntity(stateChangeEntity, StateChangeStatus.FAILURE);
            message("states.messages.change.failure", ComponentState.MessageType.FAILURE);
            return entity;

        } catch (Exception exception) {
            LOGGER.warn("Can't perform state change", exception);

            throw new StateChangeException(exception);
        }

        return entity;
    }

    @Transactional
    private <M extends StateService> Entity performChangeState(
            List<M> services, Entity entity, Entity stateChangeEntity, StateChangeEntityDescriber describer) {

        if (!canChangeState(describer, entity, stateChangeEntity.getStringField(describer.getTargetStateFieldName()))) {
            entity.setNotValid();
            return entity;
        }

        entity = hookOnValidate(entity, services, stateChangeEntity.getStringField(describer.getSourceStateFieldName()),
                stateChangeEntity.getStringField(describer.getTargetStateFieldName()), stateChangeEntity, describer);
        if (!entity.isValid()) {
//            copyErrorMessages(entity);
            return entity;
        }
        entity = changeState(entity, stateChangeEntity.getStringField(describer.getTargetStateFieldName()));

        entity = hookOnBeforeSave(entity, services, stateChangeEntity.getStringField(describer.getSourceStateFieldName()),
                stateChangeEntity.getStringField(describer.getTargetStateFieldName()), stateChangeEntity, describer);
        if (!entity.isValid()) {
            throw new EntityRuntimeException(entity);
        }
        entity = entity.getDataDefinition().save(entity);

        if (!hookOnAfterSave(entity, services, stateChangeEntity.getStringField(describer.getSourceStateFieldName()),
                stateChangeEntity.getStringField(describer.getTargetStateFieldName()), stateChangeEntity, describer)) {
            throw new EntityRuntimeException(entity);
        }

        return entity;
    }

    private Entity saveStateChangeEntity(final Entity stateChangeEntity, StateChangeStatus stateChangeStatus) {
        stateChangeEntity.setField("status", stateChangeStatus.getStringValue());

        Entity savedStateChangeEntity = saveAndValidate(stateChangeEntity);
        return savedStateChangeEntity;
    }

    private Entity buildStateChangeEntity(StateChangeEntityDescriber describer, Entity owner, String sourceState,
            String targetState) {
        final Entity stateChangeEntity = describer.getDataDefinition().create();
        final Entity shift = shiftsService.getShiftFromDateWithTime(new Date());

        stateChangeEntity.setField(describer.getDateTimeFieldName(), new Date());
        stateChangeEntity.setField(describer.getSourceStateFieldName(), sourceState);
        stateChangeEntity.setField(describer.getTargetStateFieldName(), targetState);
        stateChangeEntity.setField(describer.getShiftFieldName(), shift);
        stateChangeEntity.setField(describer.getWorkerFieldName(), securityService.getCurrentUserName());
        stateChangeEntity.setField(describer.getPhaseFieldName(), 0);
        stateChangeEntity.setField(describer.getOwnerFieldName(), owner);

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

    private <M extends StateService> Entity hookOnValidate(Entity entity, Collection<M> services, String sourceState,
            String targetState, Entity stateChangeEntity, StateChangeEntityDescriber describer) {
        for (StateService service : services) {
            entity = service.onValidate(entity, sourceState, targetState, stateChangeEntity, describer);
        }

        return entity;
    }

    private Entity changeState(Entity entity, String targetState) {
        // TODO zawsze stan będzie w tym polu?
        entity.setField("state", targetState);

        return entity;
    }

    private Entity rollbackStateChange(Entity entity, String sourceState) {
        entity.setField("state", sourceState);

        return entity;
    }

    private <M extends StateService> Entity hookOnBeforeSave(Entity entity, Collection<M> services, String sourceState,
            String targetState, Entity stateChangeEntity, StateChangeEntityDescriber describer) {
        for (StateService service : services) {
            entity = service.onBeforeSave(entity, sourceState, targetState, stateChangeEntity, describer);
        }

        return entity;
    }

    private <M extends StateService> boolean hookOnAfterSave(Entity entity, Collection<M> services, String sourceState,
            String targetState, Entity stateChangeEntity, StateChangeEntityDescriber describer) {
        for (StateService service : services) {
            entity = service.onAfterSave(entity, sourceState, targetState, stateChangeEntity, describer);
        }

        return entity.isValid();
    }

    private <M extends StateService> List<M> lookupChangeStateServices(Class<M> serviceMarker) {
        Map<String, M> stateServices = applicationContext.getBeansOfType(serviceMarker);

        List<M> services = new ArrayList<>();

        for (M service : stateServices.values()) {
            if (serviceEnabled(service)) {
                services.add(service);
            }
        }

        AnnotationAwareOrderComparator.sort(services);

        return services;
    }

    public <M extends StateService> void buildInitial(Class<M> serviceMarker, Entity entity, String initialState) {
        List<M> services = lookupChangeStateServices(serviceMarker);

        StateChangeEntityDescriber describer = services.get(0).getChangeEntityDescriber();
        Entity stateChangeEntity = buildStateChangeEntity(describer, entity, null, initialState);
        stateChangeEntity = saveStateChangeEntity(stateChangeEntity, StateChangeStatus.SUCCESSFUL);

        entity.setField(describer.getOwnerStateFieldName(), initialState);
        entity.setField(describer.getOwnerStateChangesFieldName(), Lists.newArrayList(stateChangeEntity));

    }

    private <M extends Object & StateService> boolean serviceEnabled(M service) {
        RunIfEnabled runIfEnabled = service.getClass().getAnnotation(RunIfEnabled.class);
        if (runIfEnabled == null) {
            return true;
        }
        for (String pluginIdentifier : runIfEnabled.value()) {
            if (!PluginUtils.isEnabled(pluginIdentifier)) {
                return false;
            }
        }

        return true;
    }

    private void copyMessages(Entity entity, Entity mainEntity) {
        if (mainEntity != null && mainEntity.equals(entity)) {
            return;
        }
        if (componentMessagesHolder == null) {
            return;
        }
        
        for (ErrorMessage errorMessage : entity.getGlobalErrors()) {
            componentMessagesHolder.addMessage(errorMessage);
        }
        for (ErrorMessage errorMessage : entity.getErrors().values()) {
            componentMessagesHolder.addMessage(errorMessage);
        }

        for (GlobalMessage globalMessage : entity.getGlobalMessages()) {
            componentMessagesHolder.addMessage(globalMessage);
        }
    }

    private void copyMessages(Entity entity) {
        copyMessages(entity, null);
    }

    private Entity saveAndValidate(final Entity entity) {
        if (entity == null) {
            return null;
        }
        Entity saved = entity.getDataDefinition().save(entity);
        if (!saved.isValid()) {
            throw new RuntimeException(String.format("Error on save state entity: %s", saved.getErrors()));
        }

        return saved;
    }

    private void message(String msg, ComponentState.MessageType messageType) {
        if (componentMessagesHolder != null) {
            componentMessagesHolder.addMessage(msg, messageType);
        }
    }
}
