package com.qcadoo.mes.states;

public interface StateEnum {

    String getStringValue();

    boolean canChangeTo(final StateEnum targetState);
}
