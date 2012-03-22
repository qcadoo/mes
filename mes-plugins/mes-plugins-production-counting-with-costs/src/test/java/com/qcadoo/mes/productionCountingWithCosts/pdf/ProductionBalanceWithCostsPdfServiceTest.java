package com.qcadoo.mes.productionCountingWithCosts.pdf;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.costCalculation.print.CostCalculationPdfService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.security.api.SecurityService;

public class ProductionBalanceWithCostsPdfServiceTest {

    private ProductionBalanceWithCostsPdfService productionBalanceWithCostsPdfService;

    private Locale locale = Locale.getDefault();

    @Mock
    private TranslationService translationService;

    @Mock
    private SecurityService securityService;

    @Mock
    private NumberService numberService;

    @Mock
    private ProductionBalancePdfService productionBalancePdfService;

    @Mock
    private CostCalculationPdfService costCalculationPdfService;

    @Mock
    private PdfHelper pdfHelper;

    @Mock
    private Entity balance, order;

    @Mock
    private Document document;

    @Mock
    private PdfPTable twoColumnTable, singleColumnTable, threeColumnTable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionBalanceWithCostsPdfService = new ProductionBalanceWithCostsPdfService();

        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "translationService", translationService);
        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "pdfHelper", pdfHelper);
        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "securityService", securityService);
        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "numberService", numberService);
        ReflectionTestUtils
                .setField(productionBalanceWithCostsPdfService, "costCalculationPdfService", costCalculationPdfService);
        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "productionBalancePdfService",
                productionBalancePdfService);

        given(balance.getBelongsToField("order")).willReturn(order);
        given(pdfHelper.createPanelTable(2)).willReturn(twoColumnTable);
        given(pdfHelper.createPanelTable(1)).willReturn(singleColumnTable);
        given(pdfHelper.createPanelTable(3)).willReturn(threeColumnTable);

        List<Entity> orderOperProdInComps = new LinkedList<Entity>();
        given(balance.getField("orderOperationProductInComponents")).willReturn(orderOperProdInComps);

        List<Entity> operationComponents = new LinkedList<Entity>();
        given(balance.getField("operationCostComponents")).willReturn(operationComponents);
    }

    @Test
    public void shouldAddTimeBalanceAndProductionCostsIfTypeCumulatedAndHourly() throws Exception {
        // given
        given(balance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                CalculateOperationCostMode.HOURLY.getStringValue());

        given(order.getStringField(OrderFields.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.CUMULATED.getStringValue());

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, balance, locale);

        // then
        verify(productionBalancePdfService).addTimeBalanceAsPanel(document, balance, locale);
        verify(document).add(threeColumnTable);
    }

    @Test
    public void shouldNotAddTimeBalanceAndProductionCostsIfTypeIsNotHourly() throws Exception {
        // given
        given(balance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                CalculateOperationCostMode.PIECEWORK.getStringValue());

        given(order.getStringField(OrderFields.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.CUMULATED.getStringValue());

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, balance, locale);

        // then
        verify(productionBalancePdfService, never()).addTimeBalanceAsPanel(document, balance, locale);
        verify(document, never()).add(threeColumnTable);
    }

    @Test
    public void shouldNotAddTimeBalanceAndProductionCostsIfTypeIsNotCumulated() throws Exception {
        // given
        given(balance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                CalculateOperationCostMode.HOURLY.getStringValue());

        given(order.getStringField(OrderFields.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.FOR_EACH.getStringValue());

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, balance, locale);

        // then
        verify(productionBalancePdfService, never()).addTimeBalanceAsPanel(document, balance, locale);
        verify(document, never()).add(threeColumnTable);
    }

    @Test
    public void shouldAddMachineTimeBalanceAndLaborTimeBalanceIfTypeIsHourlyAndForEach() throws Exception {
        // given
        given(balance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                CalculateOperationCostMode.HOURLY.getStringValue());

        given(order.getStringField(OrderFields.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.FOR_EACH.getStringValue());

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, balance, locale);

        // then
        verify(productionBalancePdfService).addMachineTimeBalance(document, balance, locale);
        verify(productionBalancePdfService).addLaborTimeBalance(document, balance, locale);
    }

    @Test
    public void shouldCallProductAndOperationNormsPrintingMethodNoMatterWhatIncludingrHourlyAndForEachType() throws Exception {
        // given
        given(balance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                CalculateOperationCostMode.HOURLY.getStringValue());

        given(order.getStringField(OrderFields.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.FOR_EACH.getStringValue());

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, balance, locale);

        // then
        verify(costCalculationPdfService).printMaterialAndOperationNorms(document, balance, locale);
    }

    @Test
    public void shouldCallProductAndOperationNormsPrintingMethodNoMatterWhatIncludingPieceworkAndCumulatedType() throws Exception {
        // given
        given(balance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                CalculateOperationCostMode.PIECEWORK.getStringValue());

        given(order.getStringField(OrderFields.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.CUMULATED.getStringValue());

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, balance, locale);

        // then
        verify(costCalculationPdfService).printMaterialAndOperationNorms(document, balance, locale);
    }
}
