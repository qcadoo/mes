package com.qcadoo.mes.view.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.states.AbstractContainerState;

public class FormComponentState extends AbstractContainerState {

    public static final String JSON_ENTITY_ID = "entityId";

    // TODO nagłówek - headerNew, headerEdit

    private Long entityId;

    private boolean valid = true;

    private final Map<String, Object> context = new HashMap<String, Object>();

    private final FormEventPerformer eventPerformer = new FormEventPerformer();

    private final String headerExpression;

    public FormComponentState(final String headerExpression) {
        this.headerExpression = headerExpression;
        registerEvent("clear", eventPerformer, "clear");
        registerEvent("save", eventPerformer, "save");
        registerEvent("saveAndClear", eventPerformer, "saveAndClear");
        registerEvent("initialize", eventPerformer, "initialize");
        registerEvent("reset", eventPerformer, "initialize");
        registerEvent("delete", eventPerformer, "delete");
    }

    @Override
    public void onFieldEntityIdChange(final Long entityId) {
        setFieldValue(entityId);
        eventPerformer.initialize(new String[0]);
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        if (json.has(JSON_ENTITY_ID) && !json.isNull(JSON_ENTITY_ID)) {
            entityId = json.getLong(JSON_ENTITY_ID);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initializeContext(final JSONObject json) throws JSONException {
        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            String field = iterator.next();
            if ("id".equals(field)) {
                entityId = json.getLong(field);
            } else {
                context.put(field, json.get(field));
            }
        }
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(final Long entityId) {
        this.entityId = entityId;
        requestRender();
        requestUpdateState();
        notifyEntityIdChangeListeners(entityId);
    }

    @Override
    public void setFieldValue(final Object value) {
        setEntityId((Long) value);
    }

    public Entity getEntity() {
        return null; // TODO masz entity ktore mapuje formularz, zmiany na nim wplywaja na wartosci inputow
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public Object getFieldValue() {
        return getEntityId();
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ENTITY_ID, entityId);
        return json;
    }

    protected class FormEventPerformer {

        public void saveAndClear(final String[] args) {
            save(args);
            if (isValid()) {
                clear(args);
            }
        }

        public void save(final String[] args) {
            Entity entity = new DefaultEntity(getDataDefinition().getPluginIdentifier(), getDataDefinition().getName(), entityId);

            copyFieldsToEntity(entity);
            copyContextToEntity(entity);

            try {
                entity = getDataDefinition().save(entity);

                if (!entity.isValid()) {
                    valid = false;
                    requestRender();
                    copyMessages(entity.getGlobalErrors());
                }

                copyEntityToFields(entity);

                if (entity.isValid()) {
                    setFieldValue(entity.getId());
                    addMessage("TODO - zapisano", MessageType.SUCCESS); // TODO masz
                } else {
                    addMessage("TODO - niepoprawny", MessageType.FAILURE); // TODO masz
                }
            } catch (IllegalStateException e) {
                addMessage("TODO - niezapisano - " + e.getMessage(), MessageType.FAILURE); // TODO masz
            }
        }

        public void delete(final String[] args) {
            if (entityId != null) {
                try {
                    getDataDefinition().delete(entityId);
                    addMessage("TODO - usunięto", MessageType.SUCCESS); // TODO masz
                } catch (IllegalStateException e) {
                    addMessage("TODO - nieusunięto - " + e.getMessage(), MessageType.FAILURE); // TODO masz
                }
            } else {
                addMessage("TODO - nieistnieje", MessageType.FAILURE); // TODO masz
            }

            clear(args);
        }

        public void initialize(final String[] args) {
            Entity entity = getFormEntity();

            if (entity != null) {
                copyEntityToFields(entity);
                setFieldValue(entity.getId());
            } else {
                clear(args);
            }
        }

        public void clear(final String[] args) {
            clearFields();
            setFieldValue(null);
        }

        private Entity getFormEntity() {
            if (entityId != null) {
                return getDataDefinition().get(entityId);
            } else {
                return null;
            }
        }

        private boolean isValidFormField(final String fieldName) {
            FieldDefinition field = getDataDefinition().getField(fieldName);

            if (field == null || HasManyType.class.isAssignableFrom(field.getType().getClass())) {
                return false;
            }

            return true;
        }

        private void copyEntityToFields(final Entity entity) {
            for (Map.Entry<String, FieldEntityIdChangeListener> field : getFieldEntityIdChangeListeners().entrySet()) {
                if (isValidFormField(field.getKey())) {
                    ErrorMessage message = entity.getError(field.getKey());
                    if (message == null) {
                        ((ComponentState) field.getValue()).setFieldValue(entity.getField(field.getKey()));
                    } else {
                        copyMessage((ComponentState) field.getValue(), message);
                    }
                }
            }
        }

        private void copyMessages(final List<ErrorMessage> messages) {
            for (ErrorMessage message : messages) {
                copyMessage(FormComponentState.this, message);
            }
        }

        private void copyMessage(final ComponentState componentState, final ErrorMessage message) {
            if (message != null) {
                String translation = getTranslationService().translate(message.getMessage(), getLocale());
                componentState.addMessage(translation, MessageType.FAILURE);
            }
        }

        private void clearFields() {
            for (Map.Entry<String, FieldEntityIdChangeListener> field : getFieldEntityIdChangeListeners().entrySet()) {
                if (isValidFormField(field.getKey())) {
                    ((ComponentState) field.getValue()).setFieldValue(null);
                }
            }
        }

        private void copyFieldsToEntity(final Entity entity) {
            for (Map.Entry<String, FieldEntityIdChangeListener> field : getFieldEntityIdChangeListeners().entrySet()) {
                if (isValidFormField(field.getKey())) {
                    entity.setField(field.getKey(), ((ComponentState) field.getValue()).getFieldValue());
                }
            }
        }

        private void copyContextToEntity(final Entity entity) {
            for (Map.Entry<String, Object> field : context.entrySet()) {
                if (isValidFormField(field.getKey())) {
                    entity.setField(field.getKey(), field.getValue());
                }
            }
        }

    }
}
