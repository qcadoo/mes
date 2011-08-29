package com.qcadoo.mes.costCalculation;

import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.*;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.AVERAGE;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY;
import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@RunWith(Parameterized.class)
public class ParameterizedCostCalculationServiceTest {

    private CostCalculationService costService;

    private OperationsCostCalculationService operationsCostCalculationService;

    private ProductsCostCalculationService productsCostCalculationService;

    private Map<String, Object> parameters;

    private Map<String, BigDecimal> operationCalcResultsMap, productCalcResultsMap;

    private BigDecimal expectedMaterialMarginValue, expectedProductionMarginValue, expectedTotalCosts, expectedTotalOverhead, expectedTotalMaterialCosts, expectedTotalMachineHourlyCosts, expectedTotalLaborHourlyCosts, expectedTotalPieceworkCosts, expectedTotalTechnicalProductionCosts, expectedTotalCostsPerUnit;

    private Entity source;

    private DataDefinition dd;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        //     mode, totalMachineHourly,  totalLaborHourly, totalPieceWorkCost, totalMaterialCost, productionMargin[%], materialMargin[%],  addOverhead,   quantity,     productionMarginValue, materialMarginValue,  expectedTotalCosts, expectedTotalOverhead, expectedTotalCostsPerUnit
                {HOURLY,    valueOf(200), valueOf(200),     valueOf(200),       valueOf(800),      valueOf(10),         valueOf(10),        valueOf(100),  valueOf(1),   valueOf(40),           valueOf(80),          valueOf(1420),      valueOf(220),          valueOf(1420) },
                {PIECEWORK, valueOf(200), valueOf(200),     valueOf(200),       valueOf(800),      valueOf(10),         valueOf(10),        valueOf(100),  valueOf(1),   valueOf(20),           valueOf(80),          valueOf(1200),      valueOf(200),          valueOf(1200) },
        });
    }

    @SuppressWarnings("unchecked")
    public ParameterizedCostCalculationServiceTest(
            final OperationsCostCalculationConstants mode, 
            final BigDecimal totalMachineHourly, 
            final BigDecimal totalLaborHourly,
            final BigDecimal totalPieceWorkCost, 
            final BigDecimal totalMaterialCosts, 
            final BigDecimal productionMargin, 
            final BigDecimal materialMargin, 
            final BigDecimal addOverhead, 
            final BigDecimal quantity, 
            final BigDecimal productionMarginCostValue, 
            final BigDecimal materialMarginCostValue, 
            final BigDecimal expectedTotal, 
            final BigDecimal totalOverhead, 
            final BigDecimal totalPerUnit ) {

        operationCalcResultsMap = new HashMap<String, BigDecimal>();
        operationCalcResultsMap.put("totalMachineHourlyCosts", totalMachineHourly);
        operationCalcResultsMap.put("totalLaborHourlyCosts", totalLaborHourly);
        operationCalcResultsMap.put("totalPieceworkCosts", totalPieceWorkCost);

        productCalcResultsMap = new HashMap<String, BigDecimal>();
        productCalcResultsMap.put("totalMaterialCosts", totalMaterialCosts);
        
//        operationCalcResultsMap = mock(Map.class);
//        when(operationCalcResultsMap.size()).thenReturn(3);
//        when(operationCalcResultsMap.get("totalMachineHourlyCosts")).thenReturn(totalMachineHourly);
//        when(operationCalcResultsMap.get("totalLaborHourlyCosts")).thenReturn(totalLaborHourly);
//        when(operationCalcResultsMap.get("totalPieceworkCosts")).thenReturn(totalPieceWorkCost);
//
//        productCalcResultsMap = mock(Map.class);
//        when(productCalcResultsMap.size()).thenReturn(1);
//        when(productCalcResultsMap.get("totalMaterialCosts")).thenReturn(totalMaterialCosts);

        parameters = mock(Map.class);
        when(parameters.size()).thenReturn(7);
        when(parameters.get("calculateOperationCostsMode")).thenReturn(mode);
        when(parameters.get("calculateMaterialCostsMode")).thenReturn(AVERAGE);
        when(parameters.get("productionCostMargin")).thenReturn(productionMargin);
        when(parameters.get("materialCostMargin")).thenReturn(materialMargin);
        when(parameters.get("additionalOverhead")).thenReturn(addOverhead);
        when(parameters.get("quantity")).thenReturn(quantity);
        when(parameters.get("includeTPZ")).thenReturn(false);

        expectedMaterialMarginValue = materialMarginCostValue;
        expectedProductionMarginValue = productionMarginCostValue;
        expectedTotalCosts = expectedTotal;
        expectedTotalLaborHourlyCosts = totalLaborHourly;
        expectedTotalMachineHourlyCosts = totalMachineHourly;
        expectedTotalMaterialCosts = totalMaterialCosts;
        expectedTotalPieceworkCosts = totalPieceWorkCost;
        expectedTotalCostsPerUnit = totalPerUnit;
        expectedTotalTechnicalProductionCosts = expectedTotal.subtract(totalOverhead);
        expectedTotalOverhead = totalOverhead;
    }

    @Before
    public void init() {
        costService = new CostCalculationServiceImpl();
        
        source = mock(Entity.class);
        dd = mock(DataDefinition.class);
        when(source.getDataDefinition()).thenReturn(dd);
        when(dd.get(anyLong())).thenReturn(source);

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
    public void shouldReturnCorrectResultsFromTechnology() throws Exception {
        // given
        when(dd.getName()).thenReturn(MODEL_TECHNOLOGY);
        
        // when
        Map<String, BigDecimal> resultMap = costService.calculateTotalCost(source, parameters);

        // then
        assertEquals(expectedTotalCosts, resultMap.get("totalCosts"));
        assertEquals(expectedProductionMarginValue, resultMap.get("productionCostMarginValue"));
        assertEquals(expectedMaterialMarginValue, resultMap.get("materialCostMarginValue"));
        assertEquals(expectedTotalOverhead, resultMap.get("totalOverhead"));
        assertEquals(expectedTotalMaterialCosts, resultMap.get("totalMaterialCosts"));
        assertEquals(expectedTotalMachineHourlyCosts, resultMap.get("totalMachineHourlyCosts"));
        assertEquals(expectedTotalLaborHourlyCosts, resultMap.get("totalLaborHourlyCosts"));
        assertEquals(expectedTotalPieceworkCosts, resultMap.get("totalPieceworkCosts"));
        assertEquals(expectedTotalTechnicalProductionCosts, resultMap.get("totalTechnicalProductionCosts"));
        assertEquals(expectedTotalCostsPerUnit, resultMap.get("totalCostsPerUnit"));
    }
    
    @Test
    public void shouldReturnCorrectResultsFromOrder() throws Exception {
        // given
        Entity technology = mock(Entity.class);
        when(dd.getName()).thenReturn(MODEL_ORDER);
        when(source.getBelongsToField("technology")).thenReturn(technology);
        
        // when
        Map<String, BigDecimal> resultMap = costService.calculateTotalCost(source, parameters);

        // then
        assertEquals(expectedTotalCosts, resultMap.get("totalCosts"));
        assertEquals(expectedProductionMarginValue, resultMap.get("productionCostMarginValue"));
        assertEquals(expectedMaterialMarginValue, resultMap.get("materialCostMarginValue"));
        assertEquals(expectedTotalOverhead, resultMap.get("totalOverhead"));
        assertEquals(expectedTotalMaterialCosts, resultMap.get("totalMaterialCosts"));
        assertEquals(expectedTotalMachineHourlyCosts, resultMap.get("totalMachineHourlyCosts"));
        assertEquals(expectedTotalLaborHourlyCosts, resultMap.get("totalLaborHourlyCosts"));
        assertEquals(expectedTotalPieceworkCosts, resultMap.get("totalPieceworkCosts"));
        assertEquals(expectedTotalTechnicalProductionCosts, resultMap.get("totalTechnicalProductionCosts"));
        assertEquals(expectedTotalCostsPerUnit, resultMap.get("totalCostsPerUnit"));
    }
}
