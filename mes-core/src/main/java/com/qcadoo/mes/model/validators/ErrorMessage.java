package com.qcadoo.mes.model.validators;

/**
 * Object holds validation error message.
 */
public final class ErrorMessage {

    private final String message;

    private final String[] vars;

    /**
     * Create new validation error message.
     * 
     * @param message
     *            message
     * @param vars
     *            message's vars
     */
    public ErrorMessage(final String message, final String... vars) {
        this.message = message;
        this.vars = vars;
    }

    /**
     * Return validation error message.
     * 
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return validation error message's vars.
     * 
     * @return message's vars
     */
    public String[] getVars() {
        return vars;
    }

}
