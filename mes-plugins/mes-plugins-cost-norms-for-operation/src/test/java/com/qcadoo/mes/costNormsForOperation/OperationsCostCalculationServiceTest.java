package com.qcadoo.mes.costNormsForOperation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationsCostCalculationServiceTest {

    private OperationsCostCalculationService operationCostCalculationService;

    @Before
    public void init() {
        operationCostCalculationService = new OperationsCostCalculationServiceImpl();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGetIncorrectTypeOfSource() throws Exception {
        // given
        Entity wrongEntity = mock(Entity.class);
        DataDefinition wrongDataDefinition = mock(DataDefinition.class);
        when(wrongEntity.getDataDefinition()).thenReturn(wrongDataDefinition);
        when(wrongDataDefinition.getName()).thenReturn("incorrectModel");

        // when
        operationCostCalculationService.calculateOperationsCost(wrongEntity);
    }

}
