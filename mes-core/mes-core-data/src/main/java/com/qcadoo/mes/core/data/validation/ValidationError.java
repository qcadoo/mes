package com.qcadoo.mes.core.data.validation;

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

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getVars() {
        return vars;
    }

}
