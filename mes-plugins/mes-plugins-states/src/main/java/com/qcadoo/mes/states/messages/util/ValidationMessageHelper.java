package com.qcadoo.mes.states.messages.util;

import java.util.Map.Entry;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;

public final class ValidationMessageHelper {

    private ValidationMessageHelper() {
    }

    public static void copyErrorsFromEntity(final StateChangeContext stateChangeContext, final Entity entity) {
        for (ErrorMessage globalError : entity.getGlobalErrors()) {
            stateChangeContext.addValidationError(globalError.getMessage(), globalError.getVars());
        }

        for (Entry<String, ErrorMessage> fieldErrorMessageEntry : entity.getErrors().entrySet()) {
            final ErrorMessage fieldErrorMessage = fieldErrorMessageEntry.getValue();
            stateChangeContext.addFieldValidationError(fieldErrorMessageEntry.getKey(), fieldErrorMessage.getMessage(),
                    fieldErrorMessage.getVars());
        }

    }

}
