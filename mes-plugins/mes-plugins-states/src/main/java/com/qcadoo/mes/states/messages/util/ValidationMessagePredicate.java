package com.qcadoo.mes.states.messages.util;

import static com.qcadoo.mes.states.messages.constants.StateMessageType.VALIDATION_ERROR;

import org.apache.commons.collections.Predicate;

import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.model.api.Entity;

public class ValidationMessagePredicate implements Predicate {

    @Override
    public boolean evaluate(final Object object) {
        return fieldIsEqual(object, MessageFields.TYPE, VALIDATION_ERROR.getStringValue());
    }

    protected final boolean fieldIsEqual(final Object messageObject, final String fieldName, final Object expectedValue) {
        if (!(messageObject instanceof Entity)) {
            return false;
        }
        final Entity messageEntity = (Entity) messageObject;
        final Object fieldValue = messageEntity.getField(fieldName);
        if (expectedValue == null && fieldValue == null) {
            return true;
        }
        if (expectedValue == null) {
            return false;
        }
        return expectedValue.equals(fieldValue);
    }

}
