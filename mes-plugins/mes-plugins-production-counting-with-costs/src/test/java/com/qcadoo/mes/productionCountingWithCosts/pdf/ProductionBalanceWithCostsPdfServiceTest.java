/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCountingWithCosts.pdf;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.costCalculation.print.CostCalculationPdfService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.pdf.PdfHelper;

public class ProductionBalanceWithCostsPdfServiceTest {

    private ProductionBalanceWithCostsPdfService productionBalanceWithCostsPdfService;

    private Locale locale = Locale.getDefault();

    @Mock
    private TranslationService translationService;

    @Mock
    private NumberService numberService;

    @Mock
    private PdfHelper pdfHelper;

    @Mock
    private ProductionCountingService productionCountingService;

    @Mock
    private ProductionBalancePdfService productionBalancePdfService;

    @Mock
    private CostCalculationPdfService costCalculationPdfService;

    @Mock
    private Entity productionBalance, order;

    @Mock
    private Document document;

    @Mock
    private PdfPTable twoColumnTable, singleColumnTable, threeColumnTable;

    @Mock
    private EntityList technologyOperationProductInComponents, operationCostComponents, operationPieceworkComponents;

    private static EntityList mockEntityList(final List<Entity> entities) {
        final EntityList entitiesList = mock(EntityList.class);

        given(entitiesList.iterator()).willAnswer(new Answer<Iterator<Entity>>() {

            @Override
            public Iterator<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(entities).iterator();
            }
        });

        given(entitiesList.isEmpty()).willReturn(entities.isEmpty());

        return entitiesList;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionBalanceWithCostsPdfService = new ProductionBalanceWithCostsPdfService();

        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "translationService", translationService);
        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "numberService", numberService);
        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "pdfHelper", pdfHelper);
        ReflectionTestUtils
                .setField(productionBalanceWithCostsPdfService, "productionCountingService", productionCountingService);
        ReflectionTestUtils.setField(productionBalanceWithCostsPdfService, "productionBalancePdfService",
                productionBalancePdfService);
        ReflectionTestUtils
                .setField(productionBalanceWithCostsPdfService, "costCalculationPdfService", costCalculationPdfService);

        given(productionBalance.getBelongsToField(ProductionBalanceFields.ORDER)).willReturn(order);
        given(pdfHelper.createPanelTable(2)).willReturn(twoColumnTable);
        given(pdfHelper.createPanelTable(1)).willReturn(singleColumnTable);
        given(pdfHelper.createPanelTable(3)).willReturn(threeColumnTable);

        List<Entity> empty = Lists.newArrayList();

        technologyOperationProductInComponents = mockEntityList(empty);
        operationCostComponents = mockEntityList(empty);
        operationPieceworkComponents = mockEntityList(empty);

        given(productionBalance.getHasManyField(ProductionBalanceFieldsPCWC.TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS))
                .willReturn(technologyOperationProductInComponents);

        given(productionBalance.getHasManyField(ProductionBalanceFieldsPCWC.OPERATION_COST_COMPONENTS)).willReturn(
                operationCostComponents);
        given(productionBalance.getHasManyField(ProductionBalanceFieldsPCWC.OPERATION_PIECEWORK_COST_COMPONENTS)).willReturn(
                operationPieceworkComponents);
    }

    @Test
    public void shouldAddTimeBalanceAndProductionCostsIfTypeCumulatedAndHourly() throws Exception {
        // given
        String typeOfProductionRecording = TypeOfProductionRecording.CUMULATED.getStringValue();
        String calculateOperationCostMode = CalculateOperationCostMode.HOURLY.getStringValue();

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeOfProductionRecording);
        given(productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                calculateOperationCostMode);

        given(productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)).willReturn(true);
        given(productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)).willReturn(true);

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, productionBalance, locale);

        // then
        verify(productionBalancePdfService).addTimeBalanceAsPanel(document, productionBalance, locale);
        verify(document).add(threeColumnTable);
    }

    @Test
    public void shouldNotAddTimeBalanceAndProductionCostsIfTypeIsNotHourly() throws Exception {
        // given
        String typeOfProductionRecording = TypeOfProductionRecording.CUMULATED.getStringValue();
        String calculateOperationCostMode = CalculateOperationCostMode.PIECEWORK.getStringValue();

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeOfProductionRecording);
        given(productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                calculateOperationCostMode);

        given(productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)).willReturn(true);
        given(productionCountingService.isCalculateOperationCostModePiecework(calculateOperationCostMode)).willReturn(true);

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, productionBalance, locale);

        // then
        verify(productionBalancePdfService, never()).addTimeBalanceAsPanel(document, productionBalance, locale);
        verify(document, never()).add(threeColumnTable);
    }

    @Test
    public void shouldNotAddTimeBalanceAndProductionCostsIfTypeIsNotCumulated() throws Exception {
        // given
        String typeOfProductionRecording = TypeOfProductionRecording.FOR_EACH.getStringValue();
        String calculateOperationCostMode = CalculateOperationCostMode.HOURLY.getStringValue();

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeOfProductionRecording);
        given(productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                calculateOperationCostMode);

        given(productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)).willReturn(true);
        given(productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)).willReturn(true);

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, productionBalance, locale);

        // then
        verify(productionBalancePdfService, never()).addTimeBalanceAsPanel(document, productionBalance, locale);
        verify(document, never()).add(threeColumnTable);
    }

    @Test
    public void shouldAddMachineTimeBalanceAndLaborTimeBalanceIfTypeIsHourlyAndForEach() throws Exception {
        // given
        String typeOfProductionRecording = TypeOfProductionRecording.FOR_EACH.getStringValue();
        String calculateOperationCostMode = CalculateOperationCostMode.HOURLY.getStringValue();

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeOfProductionRecording);
        given(productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                calculateOperationCostMode);

        given(productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)).willReturn(true);
        given(productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)).willReturn(true);

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, productionBalance, locale);

        // then
        verify(productionBalancePdfService).addMachineTimeBalance(document, productionBalance, locale);
        verify(productionBalancePdfService).addLaborTimeBalance(document, productionBalance, locale);
    }

    @Test
    public void shouldCallProductAndOperationNormsPrintingMethodNoMatterWhatIncludingrHourlyAndForEachType() throws Exception {
        // given
        String typeOfProductionRecording = TypeOfProductionRecording.FOR_EACH.getStringValue();
        String calculateOperationCostMode = CalculateOperationCostMode.HOURLY.getStringValue();

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeOfProductionRecording);
        given(productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                calculateOperationCostMode);

        given(productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)).willReturn(true);
        given(productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)).willReturn(true);

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, productionBalance, locale);

        // then
        verify(costCalculationPdfService).printMaterialAndOperationNorms(document, productionBalance, locale);
    }

    @Test
    public void shouldCallProductAndOperationNormsPrintingMethodNoMatterWhatIncludingPieceworkAndCumulatedType() throws Exception {
        // given
        given(productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE)).willReturn(
                CalculateOperationCostMode.PIECEWORK.getStringValue());

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.CUMULATED.getStringValue());

        // when
        productionBalanceWithCostsPdfService.buildPdfContent(document, productionBalance, locale);

        // then
        verify(costCalculationPdfService).printMaterialAndOperationNorms(document, productionBalance, locale);
    }

}
