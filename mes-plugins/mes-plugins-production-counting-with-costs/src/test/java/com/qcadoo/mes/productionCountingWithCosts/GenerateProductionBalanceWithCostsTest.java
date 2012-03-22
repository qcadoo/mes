package com.qcadoo.mes.productionCountingWithCosts;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceService;
import com.qcadoo.mes.productionCountingWithCosts.pdf.ProductionBalanceWithCostsPdfService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;

public class GenerateProductionBalanceWithCostsTest {

    private GenerateProductionBalanceWithCosts generateProductionBalanceWithCosts;

    @Mock
    private CostCalculationService costCalculationService;

    @Mock
    private NumberService numberService;

    @Mock
    private FileService fileService;

    @Mock
    private ProductionBalanceWithCostsPdfService productionBalanceWithCostsPdfService;

    @Mock
    private ProductionBalanceService productionBalanceService;

    @Mock
    private Entity balance, order, technology;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity company;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        generateProductionBalanceWithCosts = new GenerateProductionBalanceWithCosts();

        ReflectionTestUtils.setField(generateProductionBalanceWithCosts, "costCalculationService", costCalculationService);
        ReflectionTestUtils.setField(generateProductionBalanceWithCosts, "numberService", numberService);
        ReflectionTestUtils.setField(generateProductionBalanceWithCosts, "fileService", fileService);
        ReflectionTestUtils.setField(generateProductionBalanceWithCosts, "productionBalanceService", productionBalanceService);
        ReflectionTestUtils.setField(generateProductionBalanceWithCosts, "productionBalanceWithCostsPdfService",
                productionBalanceWithCostsPdfService);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);
        given(numberService.setScale(Mockito.any(BigDecimal.class))).willAnswer(new Answer<BigDecimal>() {

            @Override
            public BigDecimal answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BigDecimal number = (BigDecimal) args[0];
                return number.setScale(3, RoundingMode.HALF_EVEN);
            }
        });

        given(balance.getBelongsToField("order")).willReturn(order);
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(balance.getDataDefinition()).willReturn(dataDefinition);
    }

    @Test
    public void shouldSetQuantityTechnologyAndTechnicalProductionCostPerUnitFieldsAndSaveEntity() {
        // given
        BigDecimal quantity = BigDecimal.TEN;
        given(balance.getField("totalTechnicalProductionCosts")).willReturn(new BigDecimal(100));
        given(order.getField("plannedQuantity")).willReturn(quantity);

        // when
        generateProductionBalanceWithCosts.doTheCostsPart(balance);

        // then
        verify(balance).setField("technology", technology);
        verify(balance).setField("quantity", quantity);
        verify(balance).setField("totalTechnicalProductionCostPerUnit", BigDecimal.TEN.setScale(3, RoundingMode.HALF_EVEN));
        verify(dataDefinition).save(balance);
    }

    @Test
    public void shouldGenerateReportCorrectly() throws Exception {
        // given
        Locale locale = Locale.getDefault();
        Entity balanceWithFileName = mock(Entity.class);
        String localePrefix = "productionCounting.productionBalanceWithCosts.report.fileName";
        given(fileService.updateReportFileName(balance, "date", localePrefix)).willReturn(balanceWithFileName);
        given(productionBalanceService.getCompanyFromDB()).willReturn(company);

        // when
        generateProductionBalanceWithCosts.generateBalanceWithCostsReport(balance);

        // then
        verify(balance).setField("generatedWithCosts", Boolean.TRUE);
        verify(productionBalanceWithCostsPdfService).generateDocument(balanceWithFileName, company, locale, localePrefix);
        verify(dataDefinition).save(balance);
    }
}
