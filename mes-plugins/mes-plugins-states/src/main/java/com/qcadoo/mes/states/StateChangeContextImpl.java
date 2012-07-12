package com.qcadoo.mes.states;

import static com.qcadoo.mes.states.constants.StateChangeStatus.FAILURE;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.states.messages.util.ValidationMessageHelper;
import com.qcadoo.model.api.Entity;

public final class StateChangeContextImpl implements StateChangeContext {

    private final MessageService messageService;

    private final StateChangeEntityDescriber describer;

    private Entity entity;

    private boolean ownerValid = true;

    public StateChangeContextImpl(final Entity stateChangeEntity, final StateChangeEntityDescriber describer,
            final MessageService messageService) {
        Preconditions.checkNotNull(describer);
        Preconditions.checkNotNull(messageService);
        this.describer = describer;
        this.messageService = messageService;
        setStateChangeEntity(stateChangeEntity);
    }

    @Override
    @Transactional
    public boolean save() {
        try {
            return setStateChangeEntity(entity.getDataDefinition().save(entity));
        } catch (Exception e) {
            throw new StateChangeException(e);
        }
    }

    @Override
    public void setField(final String fieldName, final Object fieldValue) {
        entity.setField(fieldName, fieldValue);
    }

    @Override
    public StateEnum getStateEnumValue(final String fieldName) {
        return describer.parseStateEnum(entity.getStringField(fieldName));
    }

    @Override
    public int getPhase() {
        int phase = 0;
        final Object phaseFieldValue = entity.getField(describer.getPhaseFieldName());
        if (phaseFieldValue instanceof Integer) {
            phase = ((Integer) phaseFieldValue).intValue();
        }
        return phase;
    }

    @Override
    public void setPhase(final int phase) {
        setField(describer.getPhaseFieldName(), phase);
    }

    @Override
    public StateChangeStatus getStatus() {
        return StateChangeStatus.parseString(entity.getStringField(describer.getStatusFieldName()));
    }

    @Override
    public void setStatus(final StateChangeStatus status) {
        setField(describer.getStatusFieldName(), status.getStringValue());
    }

    @Override
    public StateChangeEntityDescriber getDescriber() {
        return describer;
    }

    private boolean setStateChangeEntity(final Entity stateChange) {
        Preconditions.checkNotNull(stateChange);
        if (stateChange.isValid()) {
            final Entity savedStateChange = describer.getDataDefinition().save(stateChange);
            if (savedStateChange.isValid()) {
                this.entity = savedStateChange;
                return true;
            }
        }
        markAsFailureByValidation(stateChange);
        return false;
    }

    private void markAsFailureByValidation(final Entity stateChange) {
        Entity entityToBeMarkAsFailure = this.entity;
        final Long stateChangeEntityId = stateChange.getId();
        if (entityToBeMarkAsFailure == null && stateChangeEntityId != null) {
            entityToBeMarkAsFailure = describer.getDataDefinition().get(stateChangeEntityId);
        }
        if (entityToBeMarkAsFailure == null) {
            throw new IllegalArgumentException("Given state change entity have validation errors!");
        } else {
            this.entity = entityToBeMarkAsFailure;
            ValidationMessageHelper.copyErrorsFromEntity(this, stateChange);
            setStatus(FAILURE);
            describer.getDataDefinition().save(entityToBeMarkAsFailure);
        }
    }

    @Override
    public Entity getStateChangeEntity() {
        return entity;
    }

    @Override
    public Entity getOwner() {
        return entity.getBelongsToField(describer.getOwnerFieldName());
    }

    @Override
    public void addFieldMessage(final String translationKey, final StateMessageType type, final String fieldName,
            final String... translationArgs) {
        messageService.addMessage(this, type, true, fieldName, translationKey, translationArgs);
    }

    @Override
    public void addMessage(final String translationKey, final StateMessageType type, final String... translationArgs) {
        addMessage(translationKey, type, true, translationArgs);
    }

    @Override
    public void addMessage(final String translationKey, final StateMessageType type, final boolean autoClose,
            final String... translationArgs) {
        messageService.addMessage(this, type, autoClose, null, translationKey, translationArgs);
    }

    @Override
    public void addFieldValidationError(final String fieldName, final String translationKey, final String... translationArgs) {
        messageService.addValidationError(this, fieldName, translationKey, translationArgs);
    }

    @Override
    public void addValidationError(final String translationKey, final String... translationArgs) {
        addFieldValidationError(null, translationKey, translationArgs);
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Override
    public List<Entity> getAllMessages() {
        return entity.getHasManyField(describer.getMessagesFieldName());
    }

    @Override
    public boolean setOwner(final Entity owner) {
        try {
            return performSetOwner(owner);
        } catch (Exception exception) {
            setStatus(StateChangeStatus.FAILURE);
            save();
            throw new StateChangeException(exception);
        }
    }

    @Transactional
    private boolean performSetOwner(final Entity owner) {
        if (!ownerValid) {
            return false;
        }
        boolean isValid = isEntityValid(owner);
        if (isValid) {
            final Entity savedOwner = owner.getDataDefinition().save(owner);
            isValid = isEntityValid(savedOwner);
            if (isValid) {
                entity.setField(describer.getOwnerFieldName(), savedOwner);
            }
        }
        ownerValid = isValid;
        save();
        return ownerValid;
    }

    private boolean isEntityValid(final Entity entity) {
        boolean isValid = entity.isValid();
        if (!isValid) {
            ValidationMessageHelper.copyErrorsFromEntity(this, entity);
            setStatus(StateChangeStatus.FAILURE);
        }
        return isValid;
    }

    @Override
    public boolean isOwnerValid() {
        return ownerValid;
    }

}
