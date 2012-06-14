package com.qcadoo.mes.states;

import java.util.List;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

public final class StateChangeContextImpl implements StateChangeContext {

    private final MessageService messageService;

    private final StateChangeEntityDescriber describer;

    private Entity entity;

    public StateChangeContextImpl(final Entity stateChangeEntity, final StateChangeEntityDescriber describer,
            final MessageService messageService) {
        Preconditions.checkNotNull(describer);
        Preconditions.checkNotNull(messageService);
        setEntity(stateChangeEntity);
        this.describer = describer;
        this.messageService = messageService;
    }

    @Override
    public void save() {
        setEntity(describer.getDataDefinition().save(entity));
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

    private void setEntity(final Entity entity) {
        Preconditions.checkNotNull(entity);
        this.entity = entity;
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
        messageService.addMessage(this, type, null, translationKey, translationArgs);
    }

    @Override
    public void addMessage(final String translationKey, final StateMessageType type, final String... translationArgs) {
        addFieldMessage(translationKey, type, null, translationArgs);
    }

    @Override
    public void addFieldValidationError(final String translationKey, final String fieldName, final String... translationArgs) {
        messageService.addValidationError(this, fieldName, translationKey, translationArgs);
    }

    @Override
    public void addValidationError(final String translationKey, final String... translationArgs) {
        addFieldValidationError(translationKey, null, translationArgs);
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Override
    public List<Entity> getAllMessages() {
        return entity.getHasManyField(describer.getMessagesFieldName());
    }

}
