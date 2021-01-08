/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.costNormsForMaterials.constants.ProductsCostFields;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.testing.model.NumberServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;

import static com.qcadoo.testing.model.EntityTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;

public class ProductsCostCalculationServiceTest {

    private ProductsCostCalculationService productsCostCalculationService;

    @Mock
    private Entity costCalculation;

    private Entity technology;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Before
    public void init() {
        productsCostCalculationService = new ProductsCostCalculationServiceImpl();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(productsCostCalculationService, "productQuantitiesService", productQuantitiesService);
        ReflectionTestUtils.setField(productsCostCalculationService, "numberService", NumberServiceMock.scaleAware());

        technology = mockEntity();
    }

    private Entity mockCostsHolder(final Long id, final String materialCostsUsed, final BigDecimal cost,
            final BigDecimal forQuantity) {
        Entity costsHolder = mockEntity(id);
        stubDecimalField(costsHolder, ProductsCostFields.forMode(materialCostsUsed).getStrValue(), cost);
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

    private void stubProductResults(final Map<Long, Entity> productsById) {
        given(productQuantitiesService.getProduct(anyLong())).willAnswer((Answer<Entity>) invocation -> {
            Long productId = (Long) invocation.getArguments()[0];
            return productsById.get(productId);
        });
    }

    private void stubNeededProductQuantities(final Map<Long, BigDecimal> neededProductQuantities) {
        given(productQuantitiesService.getNeededProductQuantities(any(Entity.class), any(BigDecimal.class),
                any(MrpAlgorithm.class)))
                        .willAnswer((Answer<Map<Long, BigDecimal>>) invocation -> ImmutableMap.copyOf(neededProductQuantities));
    }

    @Test
    public void shouldCalculateTotalProductsCostFromGlobalProductCosts() throws Exception {
        // given
        String materialCostsUsed = "02average";

        stubStringField(costCalculation, "materialCostsUsed", materialCostsUsed);
        stubDecimalField(costCalculation, "quantity", BigDecimal.valueOf(5L));

        Map<Long, BigDecimal> neededProductQuantities = ImmutableMap.of(1L, BigDecimal.ONE, 2L, BigDecimal.valueOf(10L));
        stubNeededProductQuantities(neededProductQuantities);

        Entity firstProduct = mockCostsHolder(1L, materialCostsUsed, BigDecimal.valueOf(100L), BigDecimal.valueOf(20));
        Entity secondProduct = mockCostsHolder(2L, materialCostsUsed, BigDecimal.valueOf(75L), BigDecimal.valueOf(3L));
        stubProductResults(ImmutableMap.of(1L, firstProduct, 2L, secondProduct));

        // when
        BigDecimal actualValue = productsCostCalculationService.calculateTotalProductsCost(costCalculation, technology);

        // then
        assertTrue(String.format("expected %s but actual value is %s", BigDecimal.valueOf(255), actualValue),
                BigDecimalUtils.valueEquals(actualValue, BigDecimal.valueOf(255)));
        assertEquals(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL, actualValue.scale());
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

        String materialCostsUsed = "02average";

        Entity product = mockProduct(1L, null, averageCost, costForNumber);

        // when
        BigDecimal result = productsCostCalculationService.calculateProductCostForGivenQuantity(product, quantity,
                materialCostsUsed);

        // then
        assertTrue(BigDecimalUtils.valueEquals(result, BigDecimal.valueOf(30L)));
    }
}
