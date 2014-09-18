package com.qcadoo.mes.costNormsForMaterials;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubDecimalField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Optional;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider.OrderMaterialCostsDataProvider;
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
        given(orderMaterialCostsDataProvider.find(eq(order.getId()), anyLong())).willReturn(Optional.fromNullable(orderMaterial));
    }

    private Entity mockMaterialCostsEntity(final BigDecimal averageCost, final BigDecimal costForNumber,
            final BigDecimal nominalCost, final BigDecimal lastPurchaseCost) {
        Entity entity = mockEntity();
        stubDecimalField(entity, TechnologyInstOperProductInCompFields.AVERAGE_COST, averageCost);
        stubDecimalField(entity, TechnologyInstOperProductInCompFields.COST_FOR_NUMBER, costForNumber);
        stubDecimalField(entity, TechnologyInstOperProductInCompFields.NOMINAL_COST, nominalCost);
        stubDecimalField(entity, TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST, lastPurchaseCost);
        return entity;
    }

    @Test
    public final void shouldThrowExceptionIfCostsCannotBeFound() {
        // given
        Long productId = 1L;
        BigDecimal newQuantity = BigDecimal.valueOf(0L).setScale(20);
        BigDecimal costForOrder = BigDecimal.valueOf(100L);
        stubOrderMaterialSearchResults(null);

        // when & then
        try {
            costNormsForMaterialsService.updateCostsForProductInOrder(order, productId, Optional.fromNullable(newQuantity),
                    Optional.fromNullable(costForOrder));
            Assert.fail();
        } catch (IllegalArgumentException ise) {
            // success
        }
    }

    @Test
    public final void shouldCopeWithZeroQuantity() {
        // given
        Long productId = 1L;
        BigDecimal newQuantity = BigDecimal.valueOf(3L);
        BigDecimal newCostForOrder = BigDecimal.valueOf(100L);

        BigDecimal averageCost = BigDecimal.ONE;
        BigDecimal oldQuantity = BigDecimal.valueOf(0L).setScale(20);
        BigDecimal nominalCost = BigDecimal.valueOf(2L);
        BigDecimal lastPurchaseCost = BigDecimal.valueOf(3L);
        Entity materialCosts = mockMaterialCostsEntity(averageCost, oldQuantity, nominalCost, lastPurchaseCost);
        stubOrderMaterialSearchResults(materialCosts);

        // when
        costNormsForMaterialsService.updateCostsForProductInOrder(order, productId, Optional.fromNullable(newQuantity),
                Optional.fromNullable(newCostForOrder));

        // then
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_ORDER, newCostForOrder);
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_NUMBER, newQuantity);
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.NOMINAL_COST, BigDecimal.valueOf(6L));
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST, BigDecimal.valueOf(9L));
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.AVERAGE_COST, BigDecimal.valueOf(3L));
        verify(materialCosts.getDataDefinition()).save(materialCosts);
    }

    @Test
    public final void shouldCopeWithZeroNewQuantity() {
        // given
        Long productId = 1L;
        BigDecimal newQuantity = BigDecimal.valueOf(0L).setScale(20);
        BigDecimal newCostForOrder = BigDecimal.valueOf(100L);

        BigDecimal averageCost = BigDecimal.ONE;
        BigDecimal oldQuantity = BigDecimal.valueOf(0L).setScale(20);
        BigDecimal nominalCost = BigDecimal.valueOf(2L);
        BigDecimal lastPurchaseCost = BigDecimal.valueOf(3L);
        Entity materialCosts = mockMaterialCostsEntity(averageCost, oldQuantity, nominalCost, lastPurchaseCost);
        stubOrderMaterialSearchResults(materialCosts);

        // when
        costNormsForMaterialsService.updateCostsForProductInOrder(order, productId, Optional.fromNullable(newQuantity),
                Optional.fromNullable(newCostForOrder));

        // then
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_ORDER, newCostForOrder);
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_NUMBER, BigDecimal.ONE);
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.NOMINAL_COST, BigDecimal.valueOf(2L));
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST, BigDecimal.valueOf(3L));
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.AVERAGE_COST, BigDecimal.valueOf(1L));
        verify(materialCosts.getDataDefinition()).save(materialCosts);
    }

    @Test
    public final void shouldUpdateCosts() {
        // given
        Long productId = 1L;
        BigDecimal newQuantity = BigDecimal.valueOf(1L);
        BigDecimal newCostForOrder = BigDecimal.valueOf(100L);

        BigDecimal averageCost = BigDecimal.valueOf(3L);
        BigDecimal oldQuantity = BigDecimal.valueOf(3L).setScale(5);
        BigDecimal nominalCost = BigDecimal.valueOf(6L);
        BigDecimal lastPurchaseCost = BigDecimal.valueOf(9L);
        Entity materialCosts = mockMaterialCostsEntity(averageCost, oldQuantity, nominalCost, lastPurchaseCost);
        stubOrderMaterialSearchResults(materialCosts);

        // when
        costNormsForMaterialsService.updateCostsForProductInOrder(order, productId, Optional.fromNullable(newQuantity),
                Optional.fromNullable(newCostForOrder));

        // then
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_ORDER, newCostForOrder);
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.COST_FOR_NUMBER, newQuantity);
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.NOMINAL_COST, BigDecimal.valueOf(2L));
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST, BigDecimal.valueOf(3L));
        verifySetDecimalField(materialCosts, TechnologyInstOperProductInCompFields.AVERAGE_COST, BigDecimal.valueOf(1L));
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
