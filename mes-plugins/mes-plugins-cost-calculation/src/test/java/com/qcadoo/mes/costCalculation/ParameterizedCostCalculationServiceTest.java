package com.qcadoo.mes.costCalculation;

import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

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
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;

@RunWith(Parameterized.class)
public class ParameterizedCostCalculationServiceTest {

    private CostCalculationService costService;

    private OperationsCostCalculationService operationsCostCalculationService;

    private ProductsCostCalculationService productsCostCalculationService;

    private Map<String, Object> parameters;

    private Map<String, BigDecimal> operationCalcResultsMap, productCalcResultsMap;

    private Entity technology, order;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        // totalMachineHourly, totalLaborHourly, totalPieceWorkCost, totalMaterialCost, includeTPZs
                { valueOf(200), valueOf(280), valueOf(480), valueOf(800), true }, });
    }

    public ParameterizedCostCalculationServiceTest(final BigDecimal totalMachineHourly, final BigDecimal totalLaborHourly,
            final BigDecimal totalPieceWorkCost, final BigDecimal totalMaterialCost, final boolean includeTPZs) {

        operationCalcResultsMap = mock(Map.class);
        when(operationCalcResultsMap.get("machineHourlyCost")).thenReturn(totalMachineHourly);
        when(operationCalcResultsMap.get("laborHourlyCost")).thenReturn(totalLaborHourly);
        when(operationCalcResultsMap.get("pieceWorkCost")).thenReturn(totalPieceWorkCost);

        productCalcResultsMap = mock(Map.class);
        when(productCalcResultsMap.get("materialCost")).thenReturn(totalMaterialCost);

        parameters = mock(Map.class);
        when(parameters.size()).thenReturn(1);
        when(parameters.get("includeTPZs")).thenReturn(includeTPZs);

        technology = mock(Entity.class);
        // TODO - stub technology model
        order = mock(Entity.class);
        // TODO - stub order model
    }

    @Before
    public void init() {
        costService = new CostCalculationServiceImpl();

        operationsCostCalculationService = mock(OperationsCostCalculationService.class);
        when(
                operationsCostCalculationService.calculateOperationsCost((Entity) anyObject(),
                        (OperationsCostCalculationConstants) anyObject(), anyBoolean(), (BigDecimal) anyObject())).thenReturn(
                operationCalcResultsMap);

        productsCostCalculationService = mock(ProductsCostCalculationService.class);
        when(
                productsCostCalculationService.calculateProductsCost((Entity) anyObject(),
                        (ProductsCostCalculationConstants) anyObject(), (BigDecimal) anyObject())).thenReturn(
                productCalcResultsMap);

        setField(costService, "operationsCalculationService", operationsCostCalculationService);
        setField(costService, "productsCostCalculationService", productsCostCalculationService);
    }

    @Test
    public void shouldReturnCorrectResults() throws Exception {
        // when
        Map<String, Object> resultsMap = costService.calculateTotalCost(technology, order, parameters);

        // then
        // TODO

    }
}
