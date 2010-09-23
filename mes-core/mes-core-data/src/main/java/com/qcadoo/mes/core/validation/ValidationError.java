package com.qcadoo.mes.core.validation;

public final class ValidationError {

    private String message;

    private final String[] vars;

    public ValidationError(final String message, final String... vars) {
        this.message = message;
        this.vars = vars;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String[] getVars() {
        return vars;
    }

}
