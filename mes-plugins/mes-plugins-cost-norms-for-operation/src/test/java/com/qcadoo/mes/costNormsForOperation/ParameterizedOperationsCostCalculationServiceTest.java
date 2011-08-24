package com.qcadoo.mes.costNormsForOperation;

/*import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.*;
 import static java.math.BigDecimal.valueOf;
 import static org.junit.Assert.*;
 import static org.mockito.AdditionalMatchers.lt;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;

 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;

 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;

 import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
 import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;

 import com.qcadoo.model.api.DataDefinition;
 import com.qcadoo.model.api.Entity;
 import com.qcadoo.model.api.EntityTree;*/

//@RunWith(Parameterized.class)
public class ParameterizedOperationsCostCalculationServiceTest {
    /*
     * private OperationsCostCalculationService operationCostCalculationService; private Entity technology; private Entity order;
     * private BigDecimal validateLaborHourlyCost, validateMachineHourlyCost, validatePieceworkCost, validateOrderQuantity,
     * validateExpectedMachine, validateExpectedLabor; private Integer validateNumberOfOperations; private
     * OperationsCostCalculationConstants validateMode; private boolean validateIncludeTPZs;
     * @Parameters public static Collection<Object[]> data() { return Arrays.asList(new Object[][] { // mode, laborHourly,
     * machineHourly, piecework, numOfOps, includeTPZs, order qtty, time, expectedMachine, expectedLabor { HOURLY, valueOf(20),
     * valueOf(10), valueOf(35), 1, false, valueOf(1), 1L, valueOf(1), valueOf(1) } }); } public
     * ParameterizedOperationsCostCalculationServiceTest(OperationsCostCalculationConstants mode, BigDecimal laborHourly,
     * BigDecimal machineHourly, BigDecimal pieceWork, Integer numOfOperations, boolean includeTPZs, BigDecimal orderQuantity,
     * Long time, BigDecimal expectedMachine, BigDecimal expectedLabor) { this.validateIncludeTPZs = includeTPZs;
     * this.validateLaborHourlyCost = laborHourly; this.validateMachineHourlyCost = machineHourly; this.validateNumberOfOperations
     * = numOfOperations; this.validatePieceworkCost = pieceWork; this.validateMode = mode; this.validateOrderQuantity =
     * orderQuantity; this.validateExpectedLabor = expectedLabor; this.validateExpectedMachine = expectedMachine; }
     * @Before public void init() { technology = mock(Entity.class); order = mock(Entity.class); EntityTree operationComponents =
     * mock(EntityTree.class); Entity operationComponent = mock(Entity.class); DataDefinition orderDataDefinition =
     * mock(DataDefinition.class); DataDefinition technologyDataDefinition = mock(DataDefinition.class);
     * when(order.getBelongsToField("technology")).thenReturn(technology);
     * when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);
     * when(order.getDataDefinition()).thenReturn(orderDataDefinition); when(orderDataDefinition.getName()).thenReturn("order");
     * when(technology.getDataDefinition()).thenReturn(technologyDataDefinition);
     * when(technologyDataDefinition.getName()).thenReturn("technology");
     * when(operationComponents.get(lt(3))).thenReturn(operationComponent);
     * when(operationComponent.getField("laborHourlyCost")).thenReturn(validateLaborHourlyCost);
     * when(operationComponent.getField("machineHourlyCost")).thenReturn(validateMachineHourlyCost);
     * when(operationComponent.getField("pieceworkCost")).thenReturn(validatePieceworkCost);
     * when(operationComponent.getField("numberOfOperations")).thenReturn(validateNumberOfOperations);
     * operationCostCalculationService = new OperationsCostCalculationServiceImpl(); }
     * @Test public void shouldReturnCorrectValuesUsingTechnology() throws Exception { //when Map<String, BigDecimal> result =
     * operationCostCalculationService .calculateOperationsCost(technology, validateMode, validateIncludeTPZs,
     * validateOrderQuantity); //then assertEquals(validateExpectedLabor, result.get("laborHourlyCost"));
     * assertEquals(validateExpectedMachine, result.get("machineHourlyCost")); }
     */
}
