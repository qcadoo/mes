package com.qcadoo.mes.states;

import org.mockito.Mockito;

import com.qcadoo.model.api.DataDefinition;

public final class MockStateChangeDescriber extends AbstractStateChangeDescriber {

    @Override
    public DataDefinition getDataDefinition() {
        return Mockito.mock(DataDefinition.class);
    }

    @Override
    public StateEnum parseStateEnum(final String stringValue) {
        return Mockito.mock(StateEnum.class);
    }

    @Override
    public void checkFields() {
    }

}
