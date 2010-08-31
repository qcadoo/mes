package com.qcadoo.mes.plugins.products.validation;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;

public class ValidationResult {

    private boolean isValid;

    private String globalMessage;

    private Map<String, String> fieldMessages;

    private Entity validEntity;

    public ValidationResult(boolean isValid) {
        this.isValid = isValid;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getGlobalMessage() {
        return globalMessage;
    }

    public void setGlobalMessage(String globalMessage) {
        this.globalMessage = globalMessage;
    }

    public Map<String, String> getFieldMessages() {
        return fieldMessages;
    }

    public void setFieldMessages(Map<String, String> fieldMessages) {
        this.fieldMessages = fieldMessages;
    }

    public Entity getValidEntity() {
        return validEntity;
    }

    public void setValidEntity(Entity validEntity) {
        this.validEntity = validEntity;
    }

}
