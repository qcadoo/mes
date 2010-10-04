package com.qcadoo.mes.model.validators;

public final class ErrorMessage {

    private String message;

    private final String[] vars;

    public ErrorMessage(final String message, final String... vars) {
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
