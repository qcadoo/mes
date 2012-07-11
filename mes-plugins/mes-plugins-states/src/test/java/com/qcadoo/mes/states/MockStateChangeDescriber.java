package com.qcadoo.mes.states;

import static org.mockito.Mockito.mock;

import org.mockito.Mockito;

import com.qcadoo.model.api.DataDefinition;

public final class MockStateChangeDescriber extends AbstractStateChangeDescriber {

    private final DataDefinition stateChangeDataDefinition;

    private final DataDefinition ownerDataDefinition;

    public MockStateChangeDescriber() {
        this(mock(DataDefinition.class), mock(DataDefinition.class));
    }

    public MockStateChangeDescriber(final DataDefinition stateChangeDD) {
        this(stateChangeDD, mock(DataDefinition.class));
    }

    public MockStateChangeDescriber(final DataDefinition stateChangeDD, final DataDefinition ownerDD) {
        this.stateChangeDataDefinition = stateChangeDD;
        this.ownerDataDefinition = ownerDD;
    }

    @Override
    public DataDefinition getDataDefinition() {
        return stateChangeDataDefinition;
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
        return ownerDataDefinition;
    }

    @Override
    public String getOwnerFieldName() {
        return "owner";
    }

}
