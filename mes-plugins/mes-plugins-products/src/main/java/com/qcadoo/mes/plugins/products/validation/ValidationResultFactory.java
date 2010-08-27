package com.qcadoo.mes.plugins.products.validation;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;

public final class ValidationResultFactory {

    private ValidationResultFactory() {

    }

    public static ValidationResult createValidResult(Entity entity) {
        ValidationResult validationResult = new ValidationResult(true);
        validationResult.setValidEntity(entity);
        return validationResult;
    }

    public static ValidationResult createInvalidResult(String globalMessage, Map<String, String> fieldMessages) {
        ValidationResult validationResult = new ValidationResult(false);
        validationResult.setGlobalMessage(globalMessage);
        validationResult.setFieldMessages(fieldMessages);
        return validationResult;
    }

}
