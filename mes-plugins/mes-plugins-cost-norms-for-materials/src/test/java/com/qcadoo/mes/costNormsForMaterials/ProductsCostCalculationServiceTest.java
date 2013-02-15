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
package com.qcadoo.mes.costNormsForMaterials;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

public class ProductsCostCalculationServiceTest {

    private ProductsCostCalculationService productsCostCalculationService;

    @Mock
    private Entity costCalculation, product, order;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Mock
    private NumberService numberService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition technologyInstanceDD;

    private Map<Entity, BigDecimal> neededProductQuantities;

    @Before
    public void init() {
        productsCostCalculationService = new ProductsCostCalculationServiceImpl();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(productsCostCalculationService, "productQuantitiesService", productQuantitiesService);
        ReflectionTestUtils.setField(productsCostCalculationService, "numberService", numberService);
        ReflectionTestUtils.setField(productsCostCalculationService, "dataDefinitionService", dataDefinitionService);
        neededProductQuantities = new HashMap<Entity, BigDecimal>();
    }

    @Test
    public void shouldCalculateTotalProductsCostFromOrderMaterialCosts() throws Exception {
        // given
        String sourceOfMaterialCosts = "02fromOrdersMaterialCosts";
        String calculateMaterialCostsMode = "01nominal";

        when(costCalculation.getStringField("calculateMaterialCostsMode")).thenReturn(calculateMaterialCostsMode);
        // when
        productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);
    }

    @Test
    public void shouldCalculateTotalProductsCostFromCurrentGlobalDefinitionsInProduct() throws Exception {
        // given
        String sourceOfMaterialCosts = "01currentGlobalDefinitionsInProduct";
        String calculateMaterialCostsMode = "02average";

        when(costCalculation.getStringField("calculateMaterialCostsMode")).thenReturn(calculateMaterialCostsMode);
        // when
        productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenCalculateTotalProductsCostForIncorrectSourceO() throws Exception {
        // given
        String sourceOfMaterialCosts = "10incorect";
        String calculateMaterialCostsMode = "02average";

        when(costCalculation.getStringField("calculateMaterialCostsMode")).thenReturn(calculateMaterialCostsMode);
        // when
        productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);
    }

    @Test
    public void shouldCalculateProductCostForGivenQuantity() throws Exception {
        // given
        BigDecimal quantity = BigDecimal.TEN;
        BigDecimal costForNumber = BigDecimal.TEN;
        BigDecimal averageCost = BigDecimal.TEN;
        String calculateMaterialCostsMode = "02average";
        when(product.getField("averageCost")).thenReturn(averageCost);
        when(product.getField("costForNumber")).thenReturn(costForNumber);
        when(numberService.getMathContext()).thenReturn(MathContext.DECIMAL64);
        // when
        productsCostCalculationService.calculateProductCostForGivenQuantity(product, quantity, calculateMaterialCostsMode);
    }

    @Test
    public void shouldGetAppropriateCostNormForProductForCurrentGlobalDefinitionsInProduct() throws Exception {
        // given
        String sourceOfMaterialCosts = "01currentGlobalDefinitionsInProduct";
        // when
        productsCostCalculationService.getAppropriateCostNormForProduct(product, order, sourceOfMaterialCosts);
    }

}
