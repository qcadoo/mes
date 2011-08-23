package com.qcadoo.mes.costNormsForOperation;

import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.HOURLY;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.lt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

public class OperationsCostCalculationServiceTest {
/*
    private OperationsCostCalculationService operationCostCalculationService;

    private Entity technology;
    private Entity order;

//    private DataDefinition orderDataDefinition;
//    private DataDefinition technologyDataDefinition;

    @Before
    public void init() {
        technology = mock(Entity.class);
        order = mock(Entity.class);
        EntityTree operationComponents = mock(EntityTree.class);
        Entity operationComponent = mock(Entity.class);
        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        DataDefinition technologyDataDefinition = mock(DataDefinition.class);
        
        when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);
        when(order.getTreeField("orderOperationComponents")).thenReturn(operationComponents);

        when(order.getDataDefinition()).thenReturn(orderDataDefinition);
        when(orderDataDefinition.getName()).thenReturn("order");

        when(technology.getDataDefinition()).thenReturn(technologyDataDefinition);
        when(technologyDataDefinition.getName()).thenReturn("technology");

        when(operationComponents.get(lt(3))).thenReturn(operationComponent);
        when(operationComponent.getField("laborHourlyCost")).thenReturn(30);
        when(operationComponent.getField("machineHourlyCost")).thenReturn(20);
        when(operationComponent.getField("pieceworkCost")).thenReturn(45);
        when(operationComponent.getField("numberOfOperations")).thenReturn(3);

        operationCostCalculationService = new OperationsCostCalculationServiceImpl();
    }
/*
    /*@Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenQuantityIsLessThanOrEqualZero() throws Exception {
        // when
        operationCostCalculationService.calculateOperationsCost(technology, HOURLY, false, BigDecimal.valueOf(0));

    }*/

    /*@Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenQuantityIsNull() throws Exception {
        // when
        operationCostCalculationService.calculateOperationsCost(technology, HOURLY, false, null);

    }*/
/*
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGetIncorrectTypeOfSource() throws Exception {
        // given
        Entity wrongEntity = mock(Entity.class);
        DataDefinition wrongDataDefinition = mock(DataDefinition.class);
        when(wrongEntity.getDataDefinition()).thenReturn(wrongDataDefinition);
        when(wrongDataDefinition.getName()).thenReturn("incorrectModel");

        // when
        operationCostCalculationService.calculateOperationsCost(wrongEntity, HOURLY, false, BigDecimal.valueOf(1));
    }

    @Test
    public void shouldReturnZeroWhenAllValueEqualsZero() throws Exception {
        // given
        Entity localOperationComponent = mock(Entity.class);
        when(localOperationComponent.getField("laborHourlyCost")).thenReturn(0);
        when(localOperationComponent.getField("machineHourlyCost")).thenReturn(0);
        when(localOperationComponent.getField("pieceworkCost")).thenReturn(0);
        when(localOperationComponent.getField("numberOfOperations")).thenReturn(1);
        // when
        HashMap<String, BigDecimal> value = operationCostCalculationService.calculateOperationsCost(technology, HOURLY, false,
                BigDecimal.valueOf(15));
        // then
        assertEquals(value.get("machineHourlyCost"), BigDecimal.valueOf(0));
    }

    // @Test
    // public void shouldReturnCorrectCostValuesForHourly() throws Exception {
    // // when
    // HashMap<String, BigDecimal> result = operationCostCalculationService.calculateOperationsCost(technology, HOURLY, false,
    // BigDecimal.valueOf(1));
    //
    // // then
    // assertEquals(BigDecimal.valueOf(50), result.get("machineHourlyCost"));
    //
    // }
    //
    // @Test
    // public void shouldReturnCorrectCostValuesForPiecework() throws Exception {
    // // when
    // HashMap<String, BigDecimal> result = operationCostCalculationService.calculateOperationsCost(technology, PIECEWORK,
    // false, BigDecimal.valueOf(1));
    //
    // // then
    // assertEquals(BigDecimal.valueOf(50), result.get("laborHourlyCost"));
    //
    // }

     */
}
