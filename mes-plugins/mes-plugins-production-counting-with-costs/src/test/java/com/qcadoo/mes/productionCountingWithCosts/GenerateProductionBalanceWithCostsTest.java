/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
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
    private ProductionBalanceService productionBalanceService;

    @Mock
    private ProductionBalanceWithCostsPdfService productionBalanceWithCostsPdfService;

    @Mock
    private Entity productionBalance, order, technology, productionLine;

    @Mock
    private DataDefinition productionBalanceDD;

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
                return number.setScale(5, RoundingMode.HALF_EVEN);
            }
        });

        given(productionBalance.getBelongsToField(ProductionBalanceFields.ORDER)).willReturn(order);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(productionBalance.getDataDefinition()).willReturn(productionBalanceDD);
    }

    @Test
    public void shouldSetQuantityTechnologyProductionLineAndTechnicalProductionCostPerUnitFieldsAndSaveEntity() {
        // given
        BigDecimal quantity = BigDecimal.TEN;
        given(productionBalance.getDecimalField(ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COSTS)).willReturn(
                BigDecimal.valueOf(100));
        given(order.getDecimalField(OrderFields.PLANNED_QUANTITY)).willReturn(quantity);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);

        // when
        generateProductionBalanceWithCosts.doTheCostsPart(productionBalance);

        // then
        verify(productionBalance).setField(ProductionBalanceFieldsPCWC.QUANTITY, quantity);
        verify(productionBalance).setField(ProductionBalanceFieldsPCWC.TECHNOLOGY, technology);
        // verify(productionBalance).setField("productionLine", productionLine);
        verify(productionBalance).setField(ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT,
                BigDecimal.TEN.setScale(5, RoundingMode.HALF_EVEN));

        verify(productionBalanceDD).save(productionBalance);
    }

    @Test
    public void shouldGenerateReportCorrectly() throws Exception {
        // given
        Locale locale = Locale.getDefault();
        Entity balanceWithFileName = mock(Entity.class);
        String localePrefix = "productionCounting.productionBalanceWithCosts.report.fileName";
        given(fileService.updateReportFileName(productionBalance, ProductionBalanceFields.DATE, localePrefix)).willReturn(
                balanceWithFileName);

        // when
        generateProductionBalanceWithCosts.generateBalanceWithCostsReport(productionBalance);

        // then
        verify(productionBalance).setField(ProductionBalanceFieldsPCWC.GENERATED_WITH_COSTS, Boolean.TRUE);
        verify(productionBalanceWithCostsPdfService).generateDocument(balanceWithFileName, locale, localePrefix);
        verify(productionBalanceDD).save(productionBalance);
    }

}
