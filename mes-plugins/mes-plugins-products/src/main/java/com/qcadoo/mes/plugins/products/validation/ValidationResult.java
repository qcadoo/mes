package com.qcadoo.mes.plugins.products.validation;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;

public class ValidationResult {

    private boolean isValid;

    private String globalMessage;

    private Map<String, String> fieldMessages;

    private Entity validEntity;

    public ValidationResult() {

    }

    public ValidationResult(boolean isValid) {
        this.isValid = isValid;
    }

    public final boolean isValid() {
        return isValid;
    }

    public final void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public final String getGlobalMessage() {
        return globalMessage;
    }

    public final void setGlobalMessage(String globalMessage) {
        this.globalMessage = globalMessage;
    }

    public final Map<String, String> getFieldMessages() {
        return fieldMessages;
    }

    public final void setFieldMessages(Map<String, String> fieldMessages) {
        this.fieldMessages = fieldMessages;
    }

    public final Entity getValidEntity() {
        return validEntity;
    }

    public final void setValidEntity(Entity validEntity) {
        this.validEntity = validEntity;
    }

}
