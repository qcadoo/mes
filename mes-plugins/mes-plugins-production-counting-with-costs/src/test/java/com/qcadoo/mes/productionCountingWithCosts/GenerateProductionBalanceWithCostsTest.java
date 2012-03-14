package com.qcadoo.mes.productionCountingWithCosts;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Observable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

public class GenerateProductionBalanceWithCostsTest {

    private GenerateProductionBalanceWithCosts generateProductionBalanceWithCosts;

    @Mock
    private CostCalculationService costCalculationService;

    @Mock
    private NumberService numberService;

    @Mock
    private Observable observable;

    @Mock
    private Entity balance, order, technology;

    @Mock
    private DataDefinition dataDefinition;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        generateProductionBalanceWithCosts = new GenerateProductionBalanceWithCosts();

        ReflectionTestUtils.setField(generateProductionBalanceWithCosts, "costCalculationService", costCalculationService);
        ReflectionTestUtils.setField(generateProductionBalanceWithCosts, "numberService", numberService);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);
        given(numberService.setScale(Mockito.any(BigDecimal.class))).willAnswer(new Answer<BigDecimal>() {

            @Override
            public BigDecimal answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BigDecimal number = (BigDecimal) args[0];
                return number.setScale(3, RoundingMode.HALF_EVEN);
            }
        });
    }

    @Ignore
    @Test
    public void shouldSetQuantityTechnologyAndTechnicalProductionCostPerUnitFieldsAndSaveEntity() {
        // given
        BigDecimal quantity = BigDecimal.TEN;
        given(balance.getBelongsToField("order")).willReturn(order);
        given(order.getField("plannedQuantity")).willReturn(quantity);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(balance.getDataDefinition()).willReturn(dataDefinition);

        given(balance.getField("totalTechnicalProductionCosts")).willReturn(new BigDecimal(100));

        // when
        generateProductionBalanceWithCosts.doTheCostsPart(balance);

        // then
        verify(balance).setField("technology", technology);
        verify(balance).setField("quantity", quantity);
        verify(balance).setField("totalTechnicalProductionCostPerUnit", BigDecimal.TEN.setScale(3, RoundingMode.HALF_EVEN));
        verify(dataDefinition).save(balance);
    }
}
