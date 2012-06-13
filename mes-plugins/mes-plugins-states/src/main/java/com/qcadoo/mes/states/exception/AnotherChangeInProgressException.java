package com.qcadoo.mes.states.exception;

public class AnotherChangeInProgressException extends StateChangeException {

    private static final long serialVersionUID = 1L;

    public AnotherChangeInProgressException() {
        super("Another state change request are still in progress.");
    }

}
