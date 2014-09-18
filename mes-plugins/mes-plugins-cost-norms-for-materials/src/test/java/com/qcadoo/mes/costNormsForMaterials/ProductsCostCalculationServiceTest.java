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
package com.qcadoo.mes.costNormsForMaterials;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDecimalField;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.costNormsForMaterials.constants.ProductsCostFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider.OrderMaterialCostsDataProvider;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.testing.model.NumberServiceMock;

public class ProductsCostCalculationServiceTest {

    private ProductsCostCalculationService productsCostCalculationService;

    @Mock
    private Entity costCalculation, order;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Mock
    private OrderMaterialCostsDataProvider orderMaterialCostsDataProvider;

    @Mock
    private DataDefinition technologyInstanceDD;

    @Before
    public void init() {
        productsCostCalculationService = new ProductsCostCalculationServiceImpl();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(productsCostCalculationService, "productQuantitiesService", productQuantitiesService);
        ReflectionTestUtils.setField(productsCostCalculationService, "numberService", NumberServiceMock.scaleAware());
        ReflectionTestUtils.setField(productsCostCalculationService, "orderMaterialCostsDataProvider",
                orderMaterialCostsDataProvider);

        stubBelongsToField(costCalculation, "order", mockEntity());
        stubBelongsToField(costCalculation, "technology", mockEntity());
    }

    private void stubOrderMaterialCostsSearchResults(final Map<Long, Entity> materialCostsByProductId) {
        given(orderMaterialCostsDataProvider.find(eq(order.getId()), anyLong())).willAnswer(new Answer<Optional<Entity>>() {

            @Override
            public Optional<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                Long productId = (Long) invocation.getArguments()[1];
                return Optional.fromNullable(materialCostsByProductId.get(productId));
            }
        });
    }

    private void verifySetDecimalField(final Entity entity, final String fieldName, final BigDecimal expectedValue) {
        ArgumentCaptor<BigDecimal> decimalCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(entity).setField(eq(fieldName), decimalCaptor.capture());
        BigDecimal actualValue = decimalCaptor.getValue();
        assertTrue(String.format("expected %s but actual value is %s", expectedValue, actualValue),
                BigDecimalUtils.valueEquals(actualValue, expectedValue));
        assertEquals(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL, actualValue.scale());
    }

    private Entity mockCostsHolder(final Long id, final String calculateMaterialCostsMode, final BigDecimal cost,
            final BigDecimal forQuantity) {
        Entity costsHolder = mockEntity(id);
        stubDecimalField(costsHolder, ProductsCostFields.forMode(calculateMaterialCostsMode).getStrValue(), cost);
        stubDecimalField(costsHolder, "costForNumber", forQuantity);
        return costsHolder;
    }

    private Entity mockProduct(final Long id, final BigDecimal nominalCost, final BigDecimal averageCost,
            final BigDecimal forQuantity) {
        Entity product = mockEntity(id);
        stubDecimalField(product, ProductFieldsCNFP.AVERAGE_COST, averageCost);
        stubDecimalField(product, ProductFieldsCNFP.NOMINAL_COST, nominalCost);
        stubDecimalField(product, ProductFieldsCNFP.COST_FOR_NUMBER, forQuantity);
        return product;
    }

    private void stubProductLookupResults(final Map<Long, Entity> productsById) {
        given(productQuantitiesService.getProduct(anyLong())).willAnswer(new Answer<Entity>() {

            @Override
            public Entity answer(final InvocationOnMock invocation) throws Throwable {
                Long productId = (Long) invocation.getArguments()[0];
                return productsById.get(productId);
            }
        });
    }

    private void stubNeededProductQuantities(final Map<Long, BigDecimal> neededProductQuantities) {
        given(
                productQuantitiesService.getNeededProductQuantities(any(Entity.class), any(BigDecimal.class),
                        any(MrpAlgorithm.class))).willAnswer(new Answer<Map<Long, BigDecimal>>() {

            @Override
            public Map<Long, BigDecimal> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableMap.copyOf(neededProductQuantities);
            }
        });
    }

    @Test
    public void shouldCalculateTotalProductsCostFromOrderMaterialCosts() throws Exception {
        // given
        String sourceOfMaterialCosts = "02fromOrdersMaterialCosts";
        String calculateMaterialCostsMode = "01nominal";

        stubStringField(costCalculation, "calculateMaterialCostsMode", calculateMaterialCostsMode);
        stubDecimalField(costCalculation, "quantity", BigDecimal.valueOf(5L));

        Map<Long, BigDecimal> neededProductQuantities = ImmutableMap.of(1L, BigDecimal.ONE, 2L, BigDecimal.valueOf(10L));
        stubNeededProductQuantities(neededProductQuantities);

        Entity firstProduct = mockCostsHolder(1L, calculateMaterialCostsMode, BigDecimal.valueOf(222L), BigDecimal.valueOf(999L));
        Entity secondProduct = mockCostsHolder(2L, calculateMaterialCostsMode, BigDecimal.valueOf(222L), BigDecimal.valueOf(999L));
        stubProductLookupResults(ImmutableMap.of(1L, firstProduct, 2L, secondProduct));

        Entity firstMaterialCosts = mockCostsHolder(1L, calculateMaterialCostsMode, BigDecimal.valueOf(100L),
                BigDecimal.valueOf(20));
        Entity secondMaterialCosts = mockCostsHolder(2L, calculateMaterialCostsMode, BigDecimal.valueOf(75L),
                BigDecimal.valueOf(3L));
        Map<Long, Entity> materialCostsByProductId = ImmutableMap.of(1L, firstMaterialCosts, 2L, secondMaterialCosts);
        stubOrderMaterialCostsSearchResults(materialCostsByProductId);

        // when
        productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);

        // then
        verifySetDecimalField(costCalculation, "totalMaterialCosts", BigDecimal.valueOf(255));
    }

    @Test
    public void shouldCalculateTotalProductsCostFromGlobalProductCosts() throws Exception {
        // given
        String sourceOfMaterialCosts = "01currentGlobalDefinitionsInProduct";
        String calculateMaterialCostsMode = "02average";

        stubStringField(costCalculation, "calculateMaterialCostsMode", calculateMaterialCostsMode);
        stubDecimalField(costCalculation, "quantity", BigDecimal.valueOf(5L));

        Map<Long, BigDecimal> neededProductQuantities = ImmutableMap.of(1L, BigDecimal.ONE, 2L, BigDecimal.valueOf(10L));
        stubNeededProductQuantities(neededProductQuantities);

        Entity firstProduct = mockCostsHolder(1L, calculateMaterialCostsMode, BigDecimal.valueOf(100L), BigDecimal.valueOf(20));
        Entity secondProduct = mockCostsHolder(2L, calculateMaterialCostsMode, BigDecimal.valueOf(75L), BigDecimal.valueOf(3L));
        stubProductLookupResults(ImmutableMap.of(1L, firstProduct, 2L, secondProduct));

        // when
        productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);

        // then
        verifySetDecimalField(costCalculation, "totalMaterialCosts", BigDecimal.valueOf(255));
    }

    @Test
    public void shouldThrowExceptionWhenCalculateTotalProductsCostForIncorrectSource() throws Exception {
        // given
        String sourceOfMaterialCosts = "10incorect";
        String calculateMaterialCostsMode = "02average";

        stubStringField(costCalculation, "calculateMaterialCostsMode", calculateMaterialCostsMode);

        // when
        try {
            productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);
            Assert.fail();
        } catch (IllegalStateException ignored) {
            // success
        }
    }

    @Test
    public void shouldCalculateProductCostUsingAppropriateCostField() throws Exception {
        for (ProductsCostFields productsCostFields : ProductsCostFields.values()) {
            performCalculationUsingGivenCostsType(productsCostFields);
        }
    }

    private void performCalculationUsingGivenCostsType(final ProductsCostFields costFields) {
        // given
        BigDecimal quantity = BigDecimal.valueOf(3L);
        BigDecimal costForNumber = BigDecimal.valueOf(5L);

        Entity product = mockProduct(1L, null, null, costForNumber);
        for (ProductsCostFields productsCostFields : ProductsCostFields.values()) {
            stubDecimalField(product, productsCostFields.getStrValue(), BigDecimal.valueOf(9999L));
        }
        stubDecimalField(product, costFields.getStrValue(), BigDecimal.valueOf(5L));

        // when
        BigDecimal result = productsCostCalculationService.calculateProductCostForGivenQuantity(product, quantity,
                costFields.getMode());

        // then
        assertTrue(BigDecimalUtils.valueEquals(result, BigDecimal.valueOf(3L)));
    }

    @Test
    public void shouldCalculateProductCostCopeWithZeroInCostForNumber() throws Exception {
        // given
        BigDecimal quantity = BigDecimal.valueOf(3L);
        BigDecimal costForNumber = BigDecimal.ZERO.setScale(30);
        BigDecimal averageCost = BigDecimal.TEN;

        String calculateMaterialCostsMode = "02average";

        Entity product = mockProduct(1L, null, averageCost, costForNumber);

        // when
        BigDecimal result = productsCostCalculationService.calculateProductCostForGivenQuantity(product, quantity,
                calculateMaterialCostsMode);

        // then
        assertTrue(BigDecimalUtils.valueEquals(result, BigDecimal.valueOf(30L)));
    }

    @Test
    public void shouldGetAppropriateCostNormForProduct1() throws Exception {
        // given
        String sourceOfMaterialCosts = "01currentGlobalDefinitionsInProduct";
        Entity product = mockEntity(202L);

        // when
        Entity results = productsCostCalculationService.getAppropriateCostNormForProduct(product, order, sourceOfMaterialCosts);

        // then
        assertEquals(product, results);
    }

    @Test
    public void shouldGetAppropriateCostNormForProduct2() throws Exception {
        // given
        String sourceOfMaterialCosts = "999yetAnotherMode";
        Entity product = mockEntity(202L);
        Entity orderMaterialCosts = mockEntity();
        stubOrderMaterialCostsSearchResults(ImmutableMap.of(product.getId(), orderMaterialCosts));

        // when
        Entity results = productsCostCalculationService.getAppropriateCostNormForProduct(product, order, sourceOfMaterialCosts);

        // then
        assertEquals(orderMaterialCosts, results);
    }

    @Test
    public void shouldGetAppropriateCostNormForProductThrowException() throws Exception {
        // given
        String sourceOfMaterialCosts = "999yetAnotherMode";
        Entity product = mockEntity(202L);
        stubOrderMaterialCostsSearchResults(ImmutableMap.<Long, Entity> of());

        // when & then
        try {
            productsCostCalculationService.getAppropriateCostNormForProduct(product, order, sourceOfMaterialCosts);
            Assert.fail();
        } catch (IllegalStateException ignored) {
            // success
        }

    }

}
