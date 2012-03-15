/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.costNormsForProduct;

import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.AVERAGE;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.LASTPURCHASE;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.NOMINAL;
import static java.math.BigDecimal.valueOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

@RunWith(Parameterized.class)
public class ParameterizedProductsCostCalculationServiceTest {

    private ProductsCostCalculationService productCostCalc;

    private ProductQuantitiesService productQuantitiesService;

    private Entity costCalculation;

    private final ProductsCostCalculationConstants calculationMode;

    private final BigDecimal averageCost, lastPurchaseCost, nominalCost, inputQuantity, orderQuantity, expectedResult;

    private final BigDecimal costForNumber;

    private NumberService numberService;

    private MathContext mathContext;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // mode, average, lastPurchase, nominal, costForNumber, input qtty, order qtty, expectedResult
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(1), valueOf(1), valueOf(1), valueOf(10) },
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(1), valueOf(2), valueOf(1), valueOf(20) },
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(1), valueOf(3), valueOf(1), valueOf(30) },
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(1), valueOf(3), valueOf(2), valueOf(60) },
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(1), valueOf(3), valueOf(3), valueOf(90) },
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(1), valueOf(3), valueOf(4), valueOf(120) },
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(2), valueOf(3), valueOf(2), valueOf(30) },
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(2), valueOf(3), valueOf(3), valueOf(45) },
                { AVERAGE, valueOf(10), valueOf(5), valueOf(15), valueOf(2), valueOf(3), valueOf(4), valueOf(60) } });
    }

    public ParameterizedProductsCostCalculationServiceTest(ProductsCostCalculationConstants mode, BigDecimal average,
            BigDecimal lastPurchase, BigDecimal nominal, BigDecimal costForNumber, BigDecimal inputQuantity,
            BigDecimal orderQuantity, BigDecimal expectedResult) {
        this.averageCost = average;
        this.expectedResult = expectedResult;
        this.inputQuantity = inputQuantity;
        this.lastPurchaseCost = lastPurchase;
        this.calculationMode = mode;
        this.nominalCost = nominal;
        this.orderQuantity = orderQuantity;
        this.costForNumber = costForNumber;
    }

    @Before
    public void init() {
        productQuantitiesService = mock(ProductQuantitiesService.class);
        numberService = mock(NumberService.class);

        costCalculation = mock(Entity.class);
        EntityTree operationComponents = mock(EntityTree.class);
        Entity operationComponent = mock(Entity.class);
        EntityList inputProducts = mock(EntityList.class);
        Entity inputProduct = mock(Entity.class);
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);

        productCostCalc = new ProductsCostCalculationServiceImpl();

        setField(productCostCalc, "productQuantitiesService", productQuantitiesService);
        setField(productCostCalc, "numberService", numberService);

        mathContext = MathContext.DECIMAL64;
        when(numberService.getMathContext()).thenReturn(mathContext);

        when(costCalculation.getField("quantity")).thenReturn(orderQuantity);
        when(costCalculation.getBelongsToField("technology")).thenReturn(technology);
        when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);
        when(costCalculation.getField("calculateMaterialCostsMode")).thenReturn(calculationMode);

        @SuppressWarnings("unchecked")
        Iterator<Entity> operationComponentsIterator = mock(Iterator.class);
        when(operationComponentsIterator.hasNext()).thenReturn(true, false);
        when(operationComponentsIterator.next()).thenReturn(operationComponent);
        when(operationComponents.iterator()).thenReturn(operationComponentsIterator);

        @SuppressWarnings("unchecked")
        Iterator<Entity> inputProductsIterator = mock(Iterator.class);
        when(inputProductsIterator.hasNext()).thenReturn(true, true, true, false);
        when(inputProductsIterator.next()).thenReturn(inputProduct);
        when(inputProducts.iterator()).thenReturn(inputProductsIterator);

        when(operationComponent.getHasManyField("operationProductInComponents")).thenReturn(inputProducts);
        when(inputProduct.getField("quantity")).thenReturn(inputQuantity);
        when(inputProduct.getBelongsToField("product")).thenReturn(product);

        when(product.getField(AVERAGE.getStrValue())).thenReturn(averageCost);
        when(product.getField(LASTPURCHASE.getStrValue())).thenReturn(lastPurchaseCost);
        when(product.getField(NOMINAL.getStrValue())).thenReturn(nominalCost);
        when(product.getField("costForNumber")).thenReturn(costForNumber);

        Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();
        productQuantities.put(product, inputQuantity.multiply(orderQuantity, numberService.getMathContext()));

        when(productQuantitiesService.getNeededProductQuantities(technology, orderQuantity, true)).thenReturn(productQuantities);

    }

    @Ignore
    @Test
    public void shouldReturnCorrectCostValuesUsingTechnology() throws Exception {
        // when
        productCostCalc.calculateProductsCost(costCalculation);

        // then
        Mockito.verify(numberService).setScale(expectedResult);
        Mockito.verify(costCalculation).setField(Mockito.eq("totalMaterialCosts"), Matchers.any(BigDecimal.class));
    }

    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testShouldReturnExceptionWhenEntityIsNull() throws Exception {
        // when
        productCostCalc.calculateProductsCost(null);
    }

    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testShouldReturnExceptionWhenQuantityIsNull() throws Exception {
        // given
        when(costCalculation.getField("quantity")).thenReturn(null);

        // when
        productCostCalc.calculateProductsCost(costCalculation);
    }
}
