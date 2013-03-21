package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.masterOrders.constants.OrderFieldsMO.MASTER_ORDER;
import static org.mockito.BDDMockito.given;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class OrderValidatorsMOTest {

    private OrderValidatorsMO orderValidatorsMO;

    @Mock
    private DataDefinition orderDD, masterDD;

    @Mock
    private TranslationService translationService;

    @Mock
    private FieldDefinition productDD, companyDD, technologyDD;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> masterOrders;

    @Mock
    private Entity order, masterOrder, product, company, technology, productMO, companyMO, technologyMO;

    @Before
    public void init() {
        orderValidatorsMO = new OrderValidatorsMO();
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(orderValidatorsMO, "translationService", translationService);
        given(order.getDataDefinition()).willReturn(orderDD);
        given(orderDD.getField(OrderFields.PRODUCT)).willReturn(productDD);
        given(orderDD.getField(OrderFields.TECHNOLOGY)).willReturn(technologyDD);
        given(orderDD.getField(OrderFields.COMPANY)).willReturn(companyDD);

        given(masterOrder.getDataDefinition()).willReturn(masterDD);

        given(order.getBelongsToField(MASTER_ORDER)).willReturn(masterOrder);
        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn("03manyProduct");

        given(order.getBelongsToField(OrderFields.PRODUCT)).willReturn(product);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);

        given(masterDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public final void shouldReturnTrueWhenMasterOrderIsEmpty() {
        // given
        given(order.getBelongsToField(MASTER_ORDER)).willReturn(null);

        // given
        boolean result = orderValidatorsMO.checkProductAndTechnology(orderDD, order);
        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldReturnTrueWhenMasterTypeIsUndefined() {
        // given
        given(order.getStringField(MASTER_ORDER)).willReturn("01undefined");

        // given
        boolean result = orderValidatorsMO.checkProductAndTechnology(orderDD, order);
        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldReturnFalseWhenTypeIsOneProductAndProductIsIncorrect() {
        Long orderProductId = 1L;
        Long masterProductId = 2L;
        // given
        given(order.getBelongsToField(MASTER_ORDER)).willReturn(masterOrder);
        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn("02oneProduct");

        given(order.getBelongsToField(OrderFields.PRODUCT)).willReturn(product);
        given(masterOrder.getBelongsToField(MasterOrderFields.PRODUCT)).willReturn(productMO);

        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(null);

        given(productMO.getId()).willReturn(masterProductId);
        given(product.getId()).willReturn(orderProductId);
        // given
        boolean result = orderValidatorsMO.checkProductAndTechnology(orderDD, order);
        // then
        Assert.assertEquals(false, result);
    }

    @Test
    public final void shouldReturnFalseWhenMasterOrdersProductDoesnotExists() {

        given(searchResult.getEntities()).willReturn(masterOrders);
        given(masterOrders.isEmpty()).willReturn(false);

        boolean result = orderValidatorsMO.checkProductAndTechnology(orderDD, order);
        // then
        Assert.assertEquals(true, result);

    }

    @Test
    public final void shouldReturnTrueWhenMasterOrderIsNotSave() {
        // given
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(null);
        // given
        boolean result = orderValidatorsMO.checkCompanyAndDeadline(orderDD, order);
        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldCheckOrderNumberReturnTrueWhenMasterOrderIsNull() {
        // given
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(null);
        // when
        boolean result = orderValidatorsMO.checkOrderNumber(orderDD, order);
        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldCheckOrderNumberReturnTrueWhenAddPreffixIsFalse() {
        // given
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(masterOrder);
        given(masterOrder.getBooleanField(MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER)).willReturn(false);
        // when
        boolean result = orderValidatorsMO.checkOrderNumber(orderDD, order);
        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldCheckOrderNumberReturnFalseWhenNumberIsIncorrect() {
        // given
        String masterOrderNumber = "ZL";
        String orderNumber = "ZXS";
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(masterOrder);
        given(masterOrder.getBooleanField(MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER)).willReturn(true);
        given(masterOrder.getStringField(MasterOrderFields.NUMBER)).willReturn(masterOrderNumber);
        given(order.getStringField(OrderFields.NUMBER)).willReturn(orderNumber);

        // when
        boolean result = orderValidatorsMO.checkOrderNumber(orderDD, order);
        // then
        Assert.assertEquals(false, result);
    }

    @Test
    public final void shouldCheckOrderNumberReturnTrueWhenNumberIsCorrect() {
        // given
        String masterOrderNumber = "ZL";
        String orderNumber = "ZLSADS";
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(masterOrder);
        given(masterOrder.getBooleanField(MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER)).willReturn(true);
        given(masterOrder.getStringField(MasterOrderFields.NUMBER)).willReturn(masterOrderNumber);
        given(order.getStringField(OrderFields.NUMBER)).willReturn(orderNumber);

        // when
        boolean result = orderValidatorsMO.checkOrderNumber(orderDD, order);
        // then
        Assert.assertEquals(true, result);
    }

}
