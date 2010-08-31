package com.qcadoo.mes.core.data.validation;

public final class ValidationError {

    private final String message;

    private final String[] vars;

    public ValidationError(final String message, final String... vars) {
        this.message = message;
        this.vars = vars;
    }

    public String getMessage() {
        return message;
    }

    public String[] getVars() {
        return vars;
    }

}
