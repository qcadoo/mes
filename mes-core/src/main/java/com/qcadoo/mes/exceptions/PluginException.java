package com.qcadoo.mes.exceptions;

public class PluginException extends Exception {

    private static final long serialVersionUID = 5503559676890157952L;

    public PluginException(final String message) {
        super(message);
    }

    public PluginException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
