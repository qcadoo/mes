package com.qcadoo.mes.costCalculation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

public class CostCalculationWithProductionBalanceTest {

    private CostCalculationService costCalculationService;

    @Mock
    private OperationsCostCalculationService operationsCostCalculationService;

    @Mock
    private ProductsCostCalculationService productsCostCalculationService;

    @Mock
    private Entity productionBalance;

    @Mock
    private NumberService numberService;

    @Mock
    private DataDefinition dataDefinition;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        costCalculationService = new CostCalculationServiceImpl();

        ReflectionTestUtils
                .setField(costCalculationService, "operationsCostCalculationService", operationsCostCalculationService);
        ReflectionTestUtils.setField(costCalculationService, "productsCostCalculationService", productsCostCalculationService);
        ReflectionTestUtils.setField(costCalculationService, "numberService", numberService);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);
        given(numberService.setScale(BigDecimal.ZERO)).willReturn(BigDecimal.ZERO);
        given(numberService.setScale(new BigDecimal(250))).willReturn(new BigDecimal(250));
        given(numberService.setScale(new BigDecimal(25))).willReturn(new BigDecimal(25));

        given(productionBalance.getDataDefinition()).willReturn(dataDefinition);
        given(productionBalance.getStringField("calculateOperationCostsMode")).willReturn("01hourly");

        given(productionBalance.getField("materialCostMargin")).willReturn(BigDecimal.ZERO);
        given(productionBalance.getField("productionCostMargin")).willReturn(BigDecimal.ZERO);
        given(productionBalance.getField("additionalOverhead")).willReturn(BigDecimal.ZERO);
        given(productionBalance.getField("quantity")).willReturn(BigDecimal.TEN);

        given(productionBalance.getField("totalMachineHourlyCosts")).willReturn(new BigDecimal(100));
        given(productionBalance.getField("totalLaborHourlyCosts")).willReturn(new BigDecimal(100));
        given(productionBalance.getField("totalMaterialCosts")).willReturn(new BigDecimal(50));
    }

    @Test
    public void shouldCalculateTotalCostsCorrectly() {
        // when
        costCalculationService.calculateTotalCost(productionBalance);

        // then
        verify(productionBalance).setField(Mockito.eq("date"), Mockito.any(java.util.Date.class));

        verify(productionBalance).setField("productionCostMarginValue", BigDecimal.ZERO);
        verify(productionBalance).setField("materialCostMarginValue", BigDecimal.ZERO);
        verify(productionBalance).setField("additionalOverheadValue", BigDecimal.ZERO);
        verify(productionBalance).setField("totalOverhead", BigDecimal.ZERO);
        verify(productionBalance).setField("totalTechnicalProductionCosts", new BigDecimal(250));
        verify(productionBalance).setField("totalCosts", new BigDecimal(250));
        verify(productionBalance).setField("costPerUnit", new BigDecimal(25));
    }
}
