package com.qcadoo.mes.newstates;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.AnotherChangeInProgressException;
import com.qcadoo.mes.states.exception.StateTransitionNotAlloweException;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
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
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.qcadoo.mes.states.constants.StateChangeStatus.IN_PROGRESS;
import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;

@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class StateExecutorService {

    private static final Logger LOG = LoggerFactory.getLogger(StateExecutorService.class);

    public static final String USER_CHANGE_STATE = "user";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ShiftsService shiftsService;

    private ComponentMessagesHolder componentMessagesHolder;

    public <M extends StateService> void changeState(final Class<M> serviceMarker, final ViewDefinitionState view,
            final String[] args) {
        componentMessagesHolder = view;

        Long userId = securityService.getCurrentUserOrQcadooBotId();
        String userName = securityService.getCurrentUserOrQcadooBotName();

        Optional<GridComponent> maybeGridComponent = view.tryFindComponentByReference(QcadooViewConstants.L_GRID);

        if (maybeGridComponent.isPresent()) {
            maybeGridComponent.get().getSelectedEntities().forEach(entity -> {
                entity = entity.getDataDefinition().getMasterModelEntity(entity.getId());

                entity.setField(USER_CHANGE_STATE, userId);

                entity = changeState(serviceMarker, entity, userName, args[0]);

                copyMessages(entity);
            });
        } else {
            Optional<FormComponent> maybeForm = view.tryFindComponentByReference(QcadooViewConstants.L_FORM);

            if (maybeForm.isPresent()) {
                FormComponent formComponent = maybeForm.get();

                Entity entity = formComponent.getEntity().getDataDefinition().get(formComponent.getEntityId());

                entity.setField(USER_CHANGE_STATE, userId);

                if (entity.isValid()) {
                    entity = changeState(serviceMarker, entity, userName, args[0]);

                    formComponent.setEntity(entity);
                }
            }
        }
    }

    public <M extends StateService> Entity changeState(final Class<M> serviceMarker, Entity entity, final String userName,
            final String targetState) {
        List<M> services = lookupChangeStateServices(serviceMarker);

        StateChangeEntityDescriber describer = services.stream().findFirst().get().getChangeEntityDescriber();
        String sourceState = entity.getStringField(describer.getOwnerStateFieldName());

        Entity stateChangeEntity = buildStateChangeEntity(describer, entity, userName, sourceState, targetState);

        try {
            stateChangeEntity = saveStateChangeContext(entity, stateChangeEntity, describer, sourceState, targetState,
                    StateChangeStatus.IN_PROGRESS);

            List<Entity> stateChanges = Lists.newArrayList();

            stateChanges.addAll(entity.getHasManyField(describer.getOwnerStateChangesFieldName()));
            stateChanges.add(stateChangeEntity);

            entity.setField(describer.getOwnerStateChangesFieldName(), stateChanges);

            entity = performChangeState(services, entity, stateChangeEntity, describer);

            if (entity.isValid()) {
                copyMessages(entity);

                saveStateChangeEntity(stateChangeEntity, StateChangeStatus.SUCCESSFUL);

                message("states.messages.change.successful", ComponentState.MessageType.SUCCESS);

                LOG.info(String.format("Change state successful. Entity name : %S id : %d. Target state : %S",
                        entity.getDataDefinition().getName(), entity.getId(),
                        stateChangeEntity.getStringField(describer.getTargetStateFieldName())));
            } else {
                saveStateChangeEntity(stateChangeEntity, StateChangeStatus.FAILURE);

                entity = rollbackStateChange(entity, sourceState);

                message("states.messages.change.failure", ComponentState.MessageType.FAILURE);

                LOG.info(String.format("Change state failure. Entity name : %S id : %d. Target state : %S",
                        entity.getDataDefinition().getName(), entity.getId(),
                        stateChangeEntity.getStringField(describer.getTargetStateFieldName())));
            }
        } catch (EntityRuntimeException entityException) {
            copyMessages(entityException.getEntity(), entity);

            entity = rollbackStateChange(entity, sourceState);

            saveStateChangeEntity(stateChangeEntity, StateChangeStatus.FAILURE);

            message("states.messages.change.failure", ComponentState.MessageType.FAILURE);

            LOG.info(String.format("State change exception. Entity name : %S id : %d. Target state : %S",
                    entity.getDataDefinition().getName(), entity.getId(), targetState));
            LOG.warn("Can't perform state change", entityException);

            return entity;
        } catch (AnotherChangeInProgressException e) {
            entity = rollbackStateChange(entity, sourceState);

            saveStateChangeEntity(stateChangeEntity, StateChangeStatus.FAILURE);

            message("states.messages.change.failure", ComponentState.MessageType.FAILURE);
            message("states.messages.change.failure.anotherChangeInProgress", ComponentState.MessageType.FAILURE);

            LOG.info(String.format("Another state change in progress. Entity name : %S id : %d. Target state : %S",
                    entity.getDataDefinition().getName(), entity.getId(), targetState));
        } catch (StateTransitionNotAlloweException e) {
            entity = rollbackStateChange(entity, sourceState);

            saveStateChangeEntity(stateChangeEntity, StateChangeStatus.FAILURE);

            message("states.messages.change.failure", ComponentState.MessageType.FAILURE);
            message("states.messages.change.failure.transitionNotAllowed", ComponentState.MessageType.FAILURE);

            LOG.info(String.format("State change - transition not allowed. Entity name : %S id : %d. Target state : %S",
                    entity.getDataDefinition().getName(), entity.getId(), targetState));
        } catch (Exception exception) {
            entity = rollbackStateChange(entity, sourceState);

            saveStateChangeEntity(stateChangeEntity, StateChangeStatus.FAILURE);

            message("states.messages.change.failure", ComponentState.MessageType.FAILURE);
            message("states.messages.change.failure.internalServerError", ComponentState.MessageType.FAILURE);

            LOG.info(String.format("State change exception. Entity name : %S id : %d. Target state : %S",
                    entity.getDataDefinition().getName(), entity.getId(), targetState));
            LOG.warn("Can't perform state change", exception);
        }

        return entity;
    }

    private Entity saveStateChangeContext(final Entity entity, Entity stateChangeEntity,
            final StateChangeEntityDescriber describer, final String _sourceState, final String _targetState,
            final StateChangeStatus status) {
        final StateEnum sourceState = describer.parseStateEnum(_sourceState);
        final StateEnum targetState = describer.parseStateEnum(_targetState);

        if (Objects.nonNull(sourceState) && !sourceState.canChangeTo(targetState)) {
            throw new StateTransitionNotAlloweException(sourceState, targetState);
        }

        checkForUnfinishedStateChange(describer, entity);

        stateChangeEntity = saveStateChangeEntity(stateChangeEntity, status);

        return stateChangeEntity;
    }

    @Transactional
    private <M extends StateService> Entity performChangeState(final List<M> services, Entity entity,
            final Entity stateChangeEntity, final StateChangeEntityDescriber describer) {
        LOG.info(String.format("Change state. Entity name : %S id : %d. Target state : %S", entity.getDataDefinition().getName(),
                entity.getId(), stateChangeEntity.getStringField(describer.getTargetStateFieldName())));

        if (!canChangeState(describer, entity, stateChangeEntity.getStringField(describer.getTargetStateFieldName()))) {
            entity.setNotValid();

            return entity;
        }

        entity = hookOnValidate(entity, services, stateChangeEntity.getStringField(describer.getSourceStateFieldName()),
                stateChangeEntity.getStringField(describer.getTargetStateFieldName()), stateChangeEntity, describer);

        if (!entity.isValid()) {
            // copyErrorMessages(entity);
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

    private Entity saveStateChangeEntity(final Entity stateChangeEntity, final StateChangeStatus stateChangeStatus) {
        stateChangeEntity.setField("status", stateChangeStatus.getStringValue());

        return saveAndValidate(stateChangeEntity);
    }

    public Entity buildStateChangeEntity(final StateChangeEntityDescriber describer, final Entity owner, String userName,
            final String sourceState, final String targetState) {
        final Entity shift = shiftsService.getShiftFromDateWithTime(new Date());

        return buildStateChangeEntity(describer, owner, userName, sourceState, targetState, shift);
    }

    public Entity buildStateChangeEntity(final StateChangeEntityDescriber describer, final Entity owner, String userName,
            final String sourceState, final String targetState, final Entity shift) {
        final Entity stateChangeEntity = describer.getDataDefinition().create();

        if (StringUtils.isEmpty(userName)) {
            userName = securityService.getCurrentUserOrQcadooBotName();
        }

        stateChangeEntity.setField(describer.getDateTimeFieldName(), new Date());
        stateChangeEntity.setField(describer.getSourceStateFieldName(), sourceState);
        stateChangeEntity.setField(describer.getTargetStateFieldName(), targetState);
        stateChangeEntity.setField(describer.getShiftFieldName(), shift);
        stateChangeEntity.setField(describer.getWorkerFieldName(), userName);
        stateChangeEntity.setField(describer.getPhaseFieldName(), 0);
        stateChangeEntity.setField(describer.getOwnerFieldName(), owner);

        return stateChangeEntity;
    }

    private <M extends StateService> boolean canChangeState(final StateChangeEntityDescriber describer, final Entity owner,
            final String targetStateString) {
        final StateEnum sourceState = describer.parseStateEnum(owner.getStringField(describer.getOwnerStateFieldName()));
        final StateEnum targetState = describer.parseStateEnum(targetStateString);

        // TODO wrzucamy błąd do encji?
        if (Objects.nonNull(sourceState) && !sourceState.canChangeTo(targetState)) {
            return false;
        }

        return true;
    }

    private <M extends StateService> Entity hookOnValidate(Entity entity, final Collection<M> services, final String sourceState,
            final String targetState, final Entity stateChangeEntity, final StateChangeEntityDescriber describer) {
        for (StateService service : services) {
            entity = service.onValidate(entity, sourceState, targetState, stateChangeEntity, describer);
        }

        return entity;
    }

    private Entity changeState(final Entity entity, final String targetState) {
        // TODO zawsze stan będzie w tym polu?
        entity.setField("state", targetState);

        return entity;
    }

    private Entity rollbackStateChange(final Entity entity, final String sourceState) {
        entity.setField("state", sourceState);
        entity.setNotValid();

        return entity;
    }

    private <M extends StateService> Entity hookOnBeforeSave(Entity entity, final Collection<M> services,
            final String sourceState, final String targetState, final Entity stateChangeEntity,
            final StateChangeEntityDescriber describer) {
        for (StateService service : services) {
            entity = service.onBeforeSave(entity, sourceState, targetState, stateChangeEntity, describer);
        }

        return entity;
    }

    private <M extends StateService> boolean hookOnAfterSave(Entity entity, final Collection<M> services,
            final String sourceState, final String targetState, final Entity stateChangeEntity,
            final StateChangeEntityDescriber describer) {
        for (StateService service : services) {
            entity = service.onAfterSave(entity, sourceState, targetState, stateChangeEntity, describer);
        }

        return entity.isValid();
    }

    private <M extends StateService> List<M> lookupChangeStateServices(final Class<M> serviceMarker) {
        Map<String, M> stateServices = applicationContext.getBeansOfType(serviceMarker);

        List<M> services = Lists.newArrayList();

        for (M service : stateServices.values()) {
            if (serviceEnabled(service)) {
                services.add(service);
            }
        }

        AnnotationAwareOrderComparator.sort(services);

        return services;
    }

    public <M extends StateService> void buildInitial(final Class<M> serviceMarker, final Entity entity,
            final String initialState) {
        List<M> services = lookupChangeStateServices(serviceMarker);

        StateChangeEntityDescriber describer = services.get(0).getChangeEntityDescriber();
        Entity stateChangeEntity = buildStateChangeEntity(describer, entity, StringUtils.EMPTY, null, initialState);

        stateChangeEntity = saveStateChangeEntity(stateChangeEntity, StateChangeStatus.SUCCESSFUL);

        entity.setField(describer.getOwnerStateFieldName(), initialState);
        entity.setField(describer.getOwnerStateChangesFieldName(), Lists.newArrayList(stateChangeEntity));
    }

    private <M extends Object & StateService> boolean serviceEnabled(final M service) {
        RunIfEnabled runIfEnabled = service.getClass().getAnnotation(RunIfEnabled.class);

        if (Objects.isNull(runIfEnabled)) {
            return true;
        }

        for (String pluginIdentifier : runIfEnabled.value()) {
            if (!PluginUtils.isEnabled(pluginIdentifier)) {
                return false;
            }
        }

        return true;
    }

    private void copyMessages(final Entity entity, final Entity mainEntity) {
        if (Objects.nonNull(mainEntity) && mainEntity.equals(entity)
                && entity.getGlobalErrors().size() == mainEntity.getGlobalErrors().size()) {
            return;
        }

        if (Objects.isNull(componentMessagesHolder)) {
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

    private void copyMessages(final Entity entity) {
        copyMessages(entity, null);
    }

    private Entity saveAndValidate(final Entity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }

        Entity saved = entity.getDataDefinition().save(entity);

        if (!saved.isValid()) {
            throw new RuntimeException(String.format("Error on save state entity: %s", saved.getErrors()));
        }

        return saved;
    }

    private void message(final String msg, final ComponentState.MessageType messageType) {
        if (Objects.nonNull(componentMessagesHolder)) {
            componentMessagesHolder.addMessage(msg, messageType);
        }
    }

    private void checkForUnfinishedStateChange(final StateChangeEntityDescriber describer, final Entity owner) {
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
