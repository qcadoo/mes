package com.qcadoo.mes.costNormsForOperation;

import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.*;
import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.lt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

//@RunWith(Parameterized.class)
public class ParameterizedOperationsCostCalculationServiceTest {
/*
    private OperationsCostCalculationService operationCostCalculationService;
    private Entity technology;
    private Entity order;

    private BigDecimal validateLaborHourlyCost,
                    validateMachineHourlyCost,
                    validatePieceworkCost,
                    validateNumberOfOperations;
    private OperationsCostCalculationConstants mode;
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // mode,    laborHourly,  machineHourly, piecework,    numOfOperations, includeTPZs
                {HOURLY,    valueOf(20),  valueOf(10),   valueOf(35),  valueOf(1),      false},
        });
    }
    
    public ParameterizedOperationsCostCalculationServiceTest() {
        
    }
    
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


     */
}
