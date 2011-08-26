package com.qcadoo.mes.costCalculation;

import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.*;
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

    private BigDecimal expectedMaterialMarginValue, expectedProductionMarginValue, expectedTotalCosts;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        //     mode, totalMachineHourly,  totalLaborHourly, totalPieceWorkCost, totalMaterialCost, productionMargin[%], materialMargin[%],  addOverhead,   quantity,     productionMarginValue, materialMarginValue,  expectedTotalCosts
                {HOURLY,    valueOf(200), valueOf(200),     valueOf(200),       valueOf(800),      Double.valueOf(10),  Double.valueOf(10), valueOf(100),  valueOf(1),   valueOf(40),           valueOf(80),          valueOf(1420)},
                {PIECEWORK, valueOf(200), valueOf(200),     valueOf(200),       valueOf(800),      Double.valueOf(10),  Double.valueOf(10), valueOf(100),  valueOf(1),   valueOf(20),           valueOf(80),          valueOf(1200)},
        });
    }

    //@SuppressWarnings("unchecked")
    public ParameterizedCostCalculationServiceTest(final OperationsCostCalculationConstants mode, final BigDecimal totalMachineHourly, final BigDecimal totalLaborHourly,
            final BigDecimal totalPieceWorkCost, final BigDecimal totalMaterialCost, final Double productionMargin, final Double materialMargin, final BigDecimal addOverhead, final BigDecimal quantity, final BigDecimal productionMarginCostValue, final BigDecimal materialMarginCostValue, final BigDecimal expectedTotal) {

        operationCalcResultsMap = mock(Map.class);
        when(operationCalcResultsMap.get("machineHourlyCost")).thenReturn(totalMachineHourly);
        when(operationCalcResultsMap.get("laborHourlyCost")).thenReturn(totalLaborHourly);
        when(operationCalcResultsMap.get("pieceWorkCost")).thenReturn(totalPieceWorkCost);

        productCalcResultsMap = mock(Map.class);
        when(productCalcResultsMap.get("materialCost")).thenReturn(totalMaterialCost);

        parameters = mock(Map.class);
        when(parameters.size()).thenReturn(1);
        when(parameters.get("mode")).thenReturn(mode);
        when(parameters.get("quantity")).thenReturn(quantity);

        technology = mock(Entity.class);
        // TODO - stub technology model
        order = mock(Entity.class);
        // TODO - stub order model
        
        expectedMaterialMarginValue = materialMarginCostValue;
        expectedProductionMarginValue = productionMarginCostValue;
        expectedTotalCosts = expectedTotal;
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

        setField(costService, "operationsCostCalculationService", operationsCostCalculationService);
        setField(costService, "productsCostCalculationService", productsCostCalculationService);
    }

    @Test
    public void shouldReturnCorrectResults() throws Exception {
        // when
        Map<String, Object> resultsMap = costService.calculateTotalCost(technology, order, parameters);
/*
        // then
        assertEquals(expectedTotalCosts, (BigDecimal) resultsMap.get("totalCosts"));
        assertEquals(expectedProductionMarginValue, (BigDecimal) resultsMap.get("productionCostMarginValue"));
        assertEquals(expectedMaterialMarginValue, (BigDecimal) resultsMap.get("materialCostMarginValue"));
*/
    }
}
