package com.qcadoo.mes.costCalculation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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

        given(numberService.setScale(Mockito.any(BigDecimal.class))).willAnswer(new Answer<BigDecimal>() {

            @Override
            public BigDecimal answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BigDecimal number = (BigDecimal) args[0];
                return number.setScale(3, RoundingMode.HALF_EVEN);
                // return number.setScale(3);
                // return number;
            }
        });

        given(productionBalance.getDataDefinition()).willReturn(dataDefinition);
        given(productionBalance.getStringField("calculateOperationCostsMode")).willReturn("01hourly");

        given(productionBalance.getField("materialCostMargin")).willReturn(BigDecimal.ONE);
        given(productionBalance.getField("productionCostMargin")).willReturn(BigDecimal.ONE);
        given(productionBalance.getField("additionalOverhead")).willReturn(BigDecimal.ONE);
        given(productionBalance.getField("quantity")).willReturn(BigDecimal.TEN);

        given(productionBalance.getField("totalMachineHourlyCosts")).willReturn(new BigDecimal(100));
        given(productionBalance.getField("totalLaborHourlyCosts")).willReturn(new BigDecimal(100));
        given(productionBalance.getField("totalMaterialCosts")).willReturn(new BigDecimal(50));
    }

    @Test
    public void shouldAssignAllValuesCorrectly() {
        // when
        costCalculationService.calculateTotalCost(productionBalance);

        // then
        verify(productionBalance).setField(Mockito.eq("date"), Mockito.any(java.util.Date.class));

        verify(productionBalance).setField("productionCostMarginValue", new BigDecimal(2).setScale(3, RoundingMode.HALF_EVEN));
        verify(productionBalance).setField("materialCostMarginValue", new BigDecimal(0.5).setScale(3, RoundingMode.HALF_EVEN));
        verify(productionBalance).setField("additionalOverheadValue", BigDecimal.ONE.setScale(3, RoundingMode.HALF_EVEN));
        verify(productionBalance).setField("totalOverhead", new BigDecimal(3.5).setScale(3, RoundingMode.HALF_EVEN));
        verify(productionBalance).setField("totalTechnicalProductionCosts",
                new BigDecimal(250).setScale(3, RoundingMode.HALF_EVEN));
        verify(productionBalance).setField("totalCosts", new BigDecimal(253.5).setScale(3, RoundingMode.HALF_EVEN));
        verify(productionBalance).setField("totalCostPerUnit", new BigDecimal(25.35).setScale(3, RoundingMode.HALF_EVEN));
    }
}
