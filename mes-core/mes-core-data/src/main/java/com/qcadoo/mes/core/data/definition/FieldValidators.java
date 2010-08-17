package com.qcadoo.mes.core.data.definition;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.FieldValidator
 */
public final class FieldValidators {

    private FieldValidators() {
    }

    public static FieldValidator notEmpty() {
        return null;
    }

    public static FieldValidator customValidator(final String expression) {
        return null;
    }

}
