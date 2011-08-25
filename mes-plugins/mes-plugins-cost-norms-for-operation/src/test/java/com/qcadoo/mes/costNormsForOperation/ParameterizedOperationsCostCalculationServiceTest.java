package com.qcadoo.mes.costNormsForOperation;

import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.HOURLY;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.PIECEWORK;
import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.productionScheduling.OrderRealizationTimeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@RunWith(Parameterized.class)
public class ParameterizedOperationsCostCalculationServiceTest {

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    private OperationsCostCalculationService operationCostCalculationService;

    private Entity technology;

    private Entity order;

    private Entity operationComponent;

    private BigDecimal validateLaborHourlyCost, validateMachineHourlyCost, validatePieceworkCost, validateOrderQuantity,
            validateExpectedMachine, validateExpectedLabor, validateNumberOfOperations, validateExpectedPieceworkCost,
            validationOutputQuantity;

    Integer realizationTime;

    private OperationsCostCalculationConstants validateMode;

    private boolean validateIncludeTPZs;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // mode, laborHourly, machineHourly, piecework, numOfOps, includeTPZs, order qtty, time, expectedMachine,
                // expectedLabor,pieceWorkCost, realizationTime, validationOutputQuantity
                { HOURLY, valueOf(20), valueOf(10), valueOf(35), valueOf(1), false, valueOf(1), 1L, valueOf(10), valueOf(20),
                        valueOf(0), 1, valueOf(5) },
                { PIECEWORK, valueOf(20), valueOf(10), valueOf(35), valueOf(1), false, valueOf(1), 1L, valueOf(0), valueOf(0),
                        valueOf(525), 1, valueOf(5) } });
    }

    public ParameterizedOperationsCostCalculationServiceTest(OperationsCostCalculationConstants mode, BigDecimal laborHourly,
            BigDecimal machineHourly, BigDecimal pieceWork, BigDecimal numOfOperations, boolean includeTPZs,
            BigDecimal orderQuantity, Long time, BigDecimal expectedMachine, BigDecimal expectedLabor,
            BigDecimal validateExpectedPieceworkCost, Integer realizationTime, BigDecimal validationOutputQuantity) {
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
        this.realizationTime = realizationTime;
        this.validationOutputQuantity = validationOutputQuantity;
    }

    @Before
    public void init() {

        // === OPERATION COMPONENT ===
        operationComponent = mock(Entity.class);
        Iterator<Entity> operationComponentsIterator = mock(Iterator.class);
        when(operationComponentsIterator.hasNext()).thenReturn(true, false);
        when(operationComponentsIterator.next()).thenReturn(operationComponent);

        when(operationComponent.getField("laborHourlyCost")).thenReturn(validateLaborHourlyCost);
        when(operationComponent.getField("machineHourlyCost")).thenReturn(validateMachineHourlyCost);
        when(operationComponent.getField("pieceworkCost")).thenReturn(validatePieceworkCost);
        when(operationComponent.getField("numberOfOperations")).thenReturn(validateNumberOfOperations);

        // === TECHNOLOGY ===
        technology = mock(Entity.class);
        EntityTree operationComponentsFromTechnology = mock(EntityTree.class);

        DataDefinition technologyDataDefinition = mock(DataDefinition.class);
        when(technology.getDataDefinition()).thenReturn(technologyDataDefinition);
        when(technologyDataDefinition.getName()).thenReturn("technology");
        when(technology.getTreeField("operationComponents")).thenReturn(operationComponentsFromTechnology);

        when(operationComponentsFromTechnology.iterator()).thenReturn(operationComponentsIterator);
        // when(operationComponentsFromTechnology.size()).thenReturn(1);

        // === ORDER ===
        order = mock(Entity.class);
        EntityTree operationComponentsFromOrder = mock(EntityTree.class);

        DataDefinition orderDataDefinition = mock(DataDefinition.class);

        when(order.getDataDefinition()).thenReturn(orderDataDefinition);
        when(orderDataDefinition.getName()).thenReturn("order");
        when(order.getBelongsToField("technology")).thenReturn(technology);
        when(order.getTreeField("orderOperationComponents")).thenReturn(operationComponentsFromOrder);
        when(operationComponentsFromOrder.iterator()).thenReturn(operationComponentsIterator);
        // when(operationComponentsFromOrder.size()).thenReturn(1);

        EntityTreeNode operationComponentEntityTreeNode = mock(EntityTreeNode.class);
        when(operationComponentsFromOrder.getRoot()).thenReturn(operationComponentEntityTreeNode);
        when(operationComponentsFromTechnology.getRoot()).thenReturn(operationComponentEntityTreeNode);

        orderRealizationTimeService = mock(OrderRealizationTimeService.class);

        when(
                orderRealizationTimeService.estimateRealizationTimeForOperation(operationComponentEntityTreeNode,
                        validateOrderQuantity, validateIncludeTPZs)).thenReturn(realizationTime);

        operationCostCalculationService = new OperationsCostCalculationServiceImpl();
        setField(operationCostCalculationService, "orderRealizationTimeService", orderRealizationTimeService);

        EntityList outputProducts = mock(EntityList.class);
        Entity outputProduct = mock(Entity.class);
        Entity product = mock(Entity.class);

        Iterator<Entity> outputProductsIterator = mock(Iterator.class);
        when(outputProductsIterator.hasNext()).thenReturn(true, true, true, false);
        when(outputProductsIterator.next()).thenReturn(outputProduct);
        when(outputProducts.iterator()).thenReturn(outputProductsIterator);

        when(operationComponent.getHasManyField("operationProductOutComponents")).thenReturn(outputProducts);
        when(outputProduct.getField("quantity")).thenReturn(validationOutputQuantity);
        when(outputProduct.getBelongsToField("product")).thenReturn(product);
    }

    @Test
    public void shouldReturnCorrectValuesUsingTechnology() throws Exception {
        // when
        Map<String, BigDecimal> result = operationCostCalculationService.calculateOperationsCost(technology, validateMode,
                validateIncludeTPZs, validateOrderQuantity);
        // then
        assertEquals(validateExpectedLabor, result.get("laborHourlyCost"));
        assertEquals(validateExpectedMachine, result.get("machineHourlyCost"));
        assertEquals(validateExpectedPieceworkCost, result.get("pieceWorkCost"));
    }

    @Test
    public void shouldReturnCorrectValuesUsingOrder() throws Exception {
        // when
        Map<String, BigDecimal> result = operationCostCalculationService.calculateOperationsCost(order, validateMode,
                validateIncludeTPZs, validateOrderQuantity);
        // then
        assertEquals(validateExpectedLabor, result.get("laborHourlyCost"));
        assertEquals(validateExpectedMachine, result.get("machineHourlyCost"));
        assertEquals(validateExpectedPieceworkCost, result.get("pieceWorkCost"));
    }

    // @Test
    // public void shouldReturnCorrectValuesUsingOperationComponent() throws Exception {
    // // when
    // Map<String, BigDecimal> result = operationCostCalculationService.calculateOperationsCost(operationComponent,
    // validateMode, validateIncludeTPZs, validateOrderQuantity);
    // // then
    // assertEquals(validateExpectedLabor, result.get("laborHourlyCost"));
    // assertEquals(validateExpectedMachine, result.get("machineHourlyCost"));
    // assertEquals(validateExpectedPieceworkCost, result.get("pieceWorkCost"));
    // }

}
