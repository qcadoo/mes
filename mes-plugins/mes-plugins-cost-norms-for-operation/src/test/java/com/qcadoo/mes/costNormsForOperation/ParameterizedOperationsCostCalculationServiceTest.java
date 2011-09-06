package com.qcadoo.mes.costNormsForOperation;

import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.HOURLY;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.PIECEWORK;
import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@RunWith(Parameterized.class)
public class ParameterizedOperationsCostCalculationServiceTest {

    private OperationsCostCalculationService operationCostCalculationService;

    private Entity costCalculation;

    private EntityTreeNode operationComponent;

    private BigDecimal validateLaborHourlyCost, validateMachineHourlyCost, validatePieceworkCost, validateOrderQuantity,
            validateExpectedMachine, validateExpectedLabor, validateNumberOfOperations, validateExpectedPieceworkCost,
            validationOutputQuantity;

    int realizationTime, expectedRealizationTime;

    private OperationsCostCalculationConstants validateMode;

    private boolean validateIncludeTPZs;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // mode, laborHourly, machineHourly, piecework, numOfOps, includeTPZs, order qtty, expectedMachine, expectedLabor
                // pieceWorkCost, validationOutputQuantity, expectedrealizationTime
                { HOURLY, valueOf(20), valueOf(10), valueOf(35), valueOf(1), false, valueOf(1), valueOf(0.05555556),
                        valueOf(0.11111112), valueOf(0), valueOf(5), 20 },
                { PIECEWORK, valueOf(20), valueOf(10), valueOf(35), valueOf(1), false, valueOf(1), valueOf(0), valueOf(0),
                        valueOf(175).setScale(4), valueOf(5), 0 } });
    }

    public ParameterizedOperationsCostCalculationServiceTest(OperationsCostCalculationConstants mode, BigDecimal laborHourly,
            BigDecimal machineHourly, BigDecimal pieceWork, BigDecimal numOfOperations, boolean includeTPZs,
            BigDecimal orderQuantity, BigDecimal expectedMachine, BigDecimal expectedLabor,
            BigDecimal validateExpectedPieceworkCost, BigDecimal validationOutputQuantity, int expectedrealizationTime) {
        this.validateIncludeTPZs = includeTPZs;
        this.validateLaborHourlyCost = laborHourly;
        this.validateMachineHourlyCost = machineHourly;
        this.validateNumberOfOperations = numOfOperations;
        this.validatePieceworkCost = pieceWork;
        this.validateMode = mode;
        this.validateOrderQuantity = orderQuantity;
        this.validateExpectedLabor = expectedLabor;
        this.validateExpectedMachine = expectedMachine;
        this.validateExpectedPieceworkCost = validateExpectedPieceworkCost;
        this.validationOutputQuantity = validationOutputQuantity;
        this.expectedRealizationTime = expectedrealizationTime;
    }

    @SuppressWarnings("unchecked")
    @Before
    public void init() {
        DataDefinition dataDefinition = mock(DataDefinition.class);
        costCalculation = mock(Entity.class);
        EntityList outputProducts = mock(EntityList.class);
        Entity outputProduct = mock(Entity.class);
        
        Iterator<Entity> outputProductsIterator = mock(Iterator.class);
        EntityTree operationComponents = mock(EntityTree.class);
        operationComponent = mock(EntityTreeNode.class);
        
        Iterator<Entity> operationComponentsIterator = mock(Iterator.class);
        
        when(costCalculation.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.getName()).thenReturn("costCalculation");
        when(costCalculation.getTreeField("calculationOperationComponents")).thenReturn(operationComponents);
        when(operationComponentsIterator.hasNext()).thenReturn(true, false);
        when(operationComponentsIterator.next()).thenReturn(operationComponent);
        when(operationComponents.iterator()).thenReturn(operationComponentsIterator);
        when(operationComponents.size()).thenReturn(1);
        when(operationComponents.getRoot()).thenReturn(operationComponent);

        when(operationComponent.getField("laborHourlyCost")).thenReturn(validateLaborHourlyCost);
        when(operationComponent.getField("machineHourlyCost")).thenReturn(validateMachineHourlyCost);
        when(operationComponent.getField("pieceworkCost")).thenReturn(validatePieceworkCost);
        when(operationComponent.getField("numberOfOperations")).thenReturn(validateNumberOfOperations);

        operationCostCalculationService = new OperationsCostCalculationServiceImpl();

        when(outputProductsIterator.hasNext()).thenReturn(true, false);

        when(outputProductsIterator.next()).thenReturn(outputProduct);
        when(outputProducts.iterator()).thenReturn(outputProductsIterator);

        when(operationComponent.getHasManyField("operationProductOutComponents")).thenReturn(outputProducts);
        when(outputProduct.getField("quantity")).thenReturn(validationOutputQuantity);

    }

    @Test
    public void shouldReturnCorrectValuesUsingTechnology() throws Exception {
        // when
//        operationCostCalculationService.calculateOperationsCost(costCalculation);
        // then
//        assertEquals(validateExpectedLabor, result.get("totalLaborHourlyCosts"));
//        assertEquals(validateExpectedMachine, result.get("totalMachineHourlyCosts"));
//        assertEquals(validateExpectedPieceworkCost, result.get("totalPieceworkCosts"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnExceptionWhenEntityIsNull() throws Exception {
        // when
        operationCostCalculationService.calculateOperationsCost(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnExceptionWhenQuantityIsNull() throws Exception {
        // given
        when(costCalculation.getField("quantity")).thenReturn(null);
        
        // when
        operationCostCalculationService.calculateOperationsCost(costCalculation);
    }

}
