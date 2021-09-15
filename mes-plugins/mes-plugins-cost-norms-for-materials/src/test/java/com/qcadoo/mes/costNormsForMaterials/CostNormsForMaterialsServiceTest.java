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

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider.OrderMaterialCostsDataProvider;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithQuantityAndCost;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.testing.model.NumberServiceMock;

public class CostNormsForMaterialsServiceTest {

    private CostNormsForMaterialsService costNormsForMaterialsService;

    @Mock
    private OrderMaterialCostsDataProvider orderMaterialCostsDataProvider;

    @Mock
    private Entity order;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        costNormsForMaterialsService = new CostNormsForMaterialsService();

        ReflectionTestUtils.setField(costNormsForMaterialsService, "orderMaterialCostsDataProvider",
                orderMaterialCostsDataProvider);
        ReflectionTestUtils.setField(costNormsForMaterialsService, "numberService", NumberServiceMock.scaleAware());

        stubOrderMaterialSearchResults(null);
    }

    private void stubOrderMaterialSearchResults(final Entity orderMaterial) {
        given(orderMaterialCostsDataProvider.findAll(anyObject())).willReturn(Collections.singletonList(orderMaterial));
    }

    private Entity mockMaterialCostsEntity() {
        Entity entity = mockEntity();
        stubBelongsToField(entity, TechnologyInstOperProductInCompFields.PRODUCT, mockEntity(1L));
        return entity;
    }

    @Test
    public final void shouldCopeWithZeroQuantity() {
        // given
        Long productId = 1L;
        BigDecimal newQuantity = BigDecimal.valueOf(3L);
        BigDecimal newCostForOrder = BigDecimal.valueOf(100L);

        Entity materialCosts = mockMaterialCostsEntity();
        stubOrderMaterialSearchResults(materialCosts);

        // when
        costNormsForMaterialsService.updateCostsForProductInOrder(order,
                Collections.singleton(new ProductWithQuantityAndCost(productId, newQuantity, newCostForOrder)));

        // then
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_ORDER, newCostForOrder);
        verify(materialCosts.getDataDefinition()).save(materialCosts);
    }

    @Test
    public final void shouldCopeWithZeroNewQuantity() {
        // given
        Long productId = 1L;
        BigDecimal newQuantity = BigDecimal.valueOf(0L).setScale(20);
        BigDecimal newCostForOrder = BigDecimal.valueOf(100L);

        Entity materialCosts = mockMaterialCostsEntity();
        stubOrderMaterialSearchResults(materialCosts);

        // when
        costNormsForMaterialsService.updateCostsForProductInOrder(order,
                Collections.singleton(new ProductWithQuantityAndCost(productId, newQuantity, newCostForOrder)));

        // then
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_ORDER, newCostForOrder);
        verify(materialCosts.getDataDefinition()).save(materialCosts);
    }

    @Test
    public final void shouldUpdateCosts() {
        // given
        Long productId = 1L;
        BigDecimal newQuantity = BigDecimal.valueOf(1L);
        BigDecimal newCostForOrder = BigDecimal.valueOf(100L);

        Entity materialCosts = mockMaterialCostsEntity();
        stubOrderMaterialSearchResults(materialCosts);

        // when
        costNormsForMaterialsService.updateCostsForProductInOrder(order,
                Collections.singleton(new ProductWithQuantityAndCost(productId, newQuantity, newCostForOrder)));

        // then
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_ORDER, newCostForOrder);
        verify(materialCosts.getDataDefinition()).save(materialCosts);
    }

    private void verifySetDecimalField(final Entity entity, final String fieldName, final BigDecimal expectedValue) {
        ArgumentCaptor<BigDecimal> decimalCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(entity).setField(eq(fieldName), decimalCaptor.capture());
        BigDecimal actualValue = decimalCaptor.getValue();
        assertTrue(String.format("expected %s but actual value is %s", expectedValue, actualValue),
                BigDecimalUtils.valueEquals(actualValue, expectedValue));
        assertEquals(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL, actualValue.scale());
    }

}
