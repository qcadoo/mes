package com.qcadoo.mes.plugins.products.validation;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;

public class ValidationResultFactory {

    private static ValidationResultFactory instance;

    private ValidationResultFactory() {

    }

    public static ValidationResultFactory getInstance() {
        if (instance == null) {
            instance = new ValidationResultFactory();
        }
        return instance;
    }

    public ValidationResult createValidResult(Entity entity) {
        ValidationResult validationResult = new ValidationResult(true);
        validationResult.setValidEntity(entity);
        return validationResult;
    }

    public ValidationResult createInvalidResult(String globalMessage, Map<String, String> fieldMessages) {
        ValidationResult validationResult = new ValidationResult(false);
        validationResult.setGlobalMessage(globalMessage);
        validationResult.setFieldMessages(fieldMessages);
        return validationResult;
    }

}
