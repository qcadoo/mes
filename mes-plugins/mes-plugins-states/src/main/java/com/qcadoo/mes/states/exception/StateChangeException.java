package com.qcadoo.mes.states.exception;

public class StateChangeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StateChangeException(final String message) {
        super(message);
    }

    public StateChangeException(final Throwable cause) {
        super(cause);
    }

    public StateChangeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
