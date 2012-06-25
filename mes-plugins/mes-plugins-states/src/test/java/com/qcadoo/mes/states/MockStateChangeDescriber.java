package com.qcadoo.mes.states;

import static org.mockito.Mockito.mock;

import org.mockito.Mockito;

import com.qcadoo.model.api.DataDefinition;

public final class MockStateChangeDescriber extends AbstractStateChangeDescriber {

    private final DataDefinition dataDefinition;

    public MockStateChangeDescriber() {
        this.dataDefinition = mock(DataDefinition.class);
    }

    public MockStateChangeDescriber(final DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    @Override
    public StateEnum parseStateEnum(final String stringValue) {
        return Mockito.mock(StateEnum.class);
    }

    @Override
    public void checkFields() {
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return mock(DataDefinition.class);
    }

}
