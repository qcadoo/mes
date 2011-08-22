package com.qcadoo.mes.costNormsForOperation;

import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.lt;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;


public class OperationsCostCalculationServiceTest {

    private OperationsCostCalculationService operationCostCalculationService;
    private Entity technology;
    private EntityTree operationComponents;
    private Entity order;
    private Entity operationComponent;
    private DataDefinition orderDataDefinition;
    private DataDefinition technologyDataDefinition;
    
    @Before
    public void init() {
        technology = mock(Entity.class);
        order = mock(Entity.class);
        operationComponents = mock(EntityTree.class);
        operationComponent = mock(Entity.class);

        orderDataDefinition = mock(DataDefinition.class);
        technologyDataDefinition = mock(DataDefinition.class);
        
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
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionWhenQuantityIsLessThanOrEqualZero() throws Exception {
        //when
        operationCostCalculationService.calculateOperationsCost(technology, HOURLY, false, BigDecimal.valueOf(0));
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionWhenQuantityIsNull() throws Exception {
        //when
        operationCostCalculationService.calculateOperationsCost(technology, HOURLY, false, null);
        
    }

    @Test
    public void shouldReturnCorrectCostValues() throws Exception {
        //when
        BigDecimal result = operationCostCalculationService.calculateOperationsCost(technology, HOURLY, false, BigDecimal.valueOf(1));
        
        //then
        assertEquals(BigDecimal.valueOf(50), result);
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGetIncorrectTypeOfSource() throws Exception {
        //given
        Entity wrongEntity = mock(Entity.class);
        DataDefinition wrongDataDefinition = mock(DataDefinition.class);
        when(wrongEntity.getDataDefinition()).thenReturn(wrongDataDefinition);
        when(wrongDataDefinition.getName()).thenReturn("incorrectModel");

        //when
        operationCostCalculationService.calculateOperationsCost(wrongEntity, HOURLY, false, BigDecimal.valueOf(1));
        
    }
}
