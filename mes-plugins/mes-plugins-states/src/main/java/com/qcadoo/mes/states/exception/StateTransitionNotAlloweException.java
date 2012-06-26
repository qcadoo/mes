package com.qcadoo.mes.states.exception;

import com.qcadoo.mes.states.StateEnum;

public class StateTransitionNotAlloweException extends StateChangeException {

    private static final long serialVersionUID = 1L;

    public StateTransitionNotAlloweException(final StateEnum sourceState, final StateEnum targetState) {
        super("State change transition from " + sourceState + " to " + targetState + " is not permited.");
    }

}
