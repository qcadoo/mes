package com.qcadoo.mes.newview.components;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.newview.AbstractContainerState;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.FieldEntityIdChangeListener;

public class FormComponentState extends AbstractContainerState {

    private Long value;

    private boolean valid = true;

    public FormComponentState() {
        FormEventPerformer eventPerformer = new FormEventPerformer();
        registerEvent("clear", eventPerformer, "clear");
        registerEvent("save", eventPerformer, "save");
        registerEvent("saveAndClear", eventPerformer, "saveAndClear");
        registerEvent("initialize", eventPerformer, "initialize");
        registerEvent("reset", eventPerformer, "initialize");
        registerEvent("delete", eventPerformer, "delete");
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        value = json.getLong(JSON_VALUE);
    }

    @Override
    public void setFieldValue(final Object value) {
        this.value = (Long) value;
        requestRender();
        requestUpdateState();
        notifyEntityIdChangeListeners((Long) value);
    }

    private void setNotValid() {
        valid = false;
        requestRender();
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public Object getFieldValue() {
        return value;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_VALUE, value);
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
            Entity entity = new DefaultEntity(getDataDefinition().getPluginIdentifier(), getDataDefinition().getName(), value);

            copyFieldsToEntity(entity);

            entity = getDataDefinition().save(entity);

            if (!entity.isValid()) {
                setNotValid();
                copyMessages(entity.getGlobalErrors());
            }

            copyEntityToFields(entity);

            if (entity.isValid()) {
                setFieldValue(entity.getId());
            }
        }

        public void delete(final String[] args) {
            if (value != null) {
                getDataDefinition().delete(value);
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
            if (value != null) {
                return getDataDefinition().get(value);
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

    }
}
