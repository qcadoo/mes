package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.masterOrders.constants.OrderFieldsMO.MASTER_ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCT;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static org.mockito.Mockito.when;

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
    private DataDefinition dataDefinition;

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
        when(order.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.getField(OrderFields.PRODUCT)).thenReturn(productDD);
        when(dataDefinition.getField(OrderFields.TECHNOLOGY)).thenReturn(technologyDD);
        when(dataDefinition.getField(OrderFields.COMPANY)).thenReturn(companyDD);

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public final void shouldReturnTrueWhenMasterOrderIsEmpty() {
        // given
        when(order.getBelongsToField(MASTER_ORDER)).thenReturn(null);

        // when
        boolean result = orderValidatorsMO.checkOrderFieldWithMasterOrders(dataDefinition, order);
        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldReturnTrueWhenMasterTypeIsUndefined() {
        // given
        when(order.getStringField(MASTER_ORDER)).thenReturn("01undefined");

        // when
        boolean result = orderValidatorsMO.checkOrderFieldWithMasterOrders(dataDefinition, order);
        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldReturnFalseWhenTypeIsOneProductAndProductIsIncorrect() {
        Long orderProductId = 1L;
        Long masterProductId = 2L;
        // given
        when(order.getBelongsToField(MASTER_ORDER)).thenReturn(masterOrder);
        when(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).thenReturn("02oneProduct");

        when(order.getBelongsToField(OrderFields.PRODUCT)).thenReturn(product);
        when(masterOrder.getBelongsToField(MasterOrderFields.PRODUCT)).thenReturn(productMO);

        when(order.getBelongsToField(OrderFields.TECHNOLOGY)).thenReturn(null);

        when(productMO.getId()).thenReturn(masterProductId);
        when(product.getId()).thenReturn(orderProductId);
        // when
        boolean result = orderValidatorsMO.checkOrderFieldWithMasterOrders(dataDefinition, order);
        // then
        Assert.assertEquals(false, result);
    }

    @Test
    public final void shouldReturnFalseWhenMasterOrdersProductDoesnotExists() {
        when(order.getBelongsToField(MASTER_ORDER)).thenReturn(masterOrder);
        when(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).thenReturn("03manyProduct");

        when(order.getBelongsToField(OrderFields.PRODUCT)).thenReturn(product);
        when(order.getBelongsToField(OrderFields.TECHNOLOGY)).thenReturn(technology);

        SearchCriterion criterion = SearchRestrictions.belongsTo(PRODUCT, product);
        SearchCriterion criterion2 = SearchRestrictions.belongsTo(TECHNOLOGY, technology);

        when(dataDefinition.find()).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.list()).thenReturn(searchResult);

        when(searchResult.getEntities()).thenReturn(masterOrders);
        when(masterOrders.isEmpty()).thenReturn(false);

        boolean result = orderValidatorsMO.checkOrderFieldWithMasterOrders(dataDefinition, order);
        // then
        Assert.assertEquals(false, result);

    }

    @Test
    public final void shouldReturnTrueWhenAllOfFieldsIsTheSame() {
        Long orderProductId = 1L;
        Long masterProductId = 1L;
        Long orderTechnologyId = 1L;
        Long masterTechnologyId = 1L;
        Long orderCompanyId = 1L;
        Long masterCompanyId = 1L;
        // given
        when(order.getBelongsToField(MASTER_ORDER)).thenReturn(masterOrder);
        when(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).thenReturn("02oneProduct");
        when(masterOrder.getBelongsToField(MasterOrderFields.PRODUCT)).thenReturn(productMO);
        when(order.getBelongsToField(OrderFields.PRODUCT)).thenReturn(product);

        when(productMO.getId()).thenReturn(masterProductId);
        when(product.getId()).thenReturn(orderProductId);

        when(order.getBelongsToField(MASTER_ORDER)).thenReturn(masterOrder);
        when(masterOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY)).thenReturn(technologyMO);
        when(order.getBelongsToField(OrderFields.TECHNOLOGY)).thenReturn(technology);

        when(technologyMO.getId()).thenReturn(masterTechnologyId);
        when(technology.getId()).thenReturn(orderTechnologyId);

        when(order.getBelongsToField(MASTER_ORDER)).thenReturn(masterOrder);
        when(masterOrder.getBelongsToField(MasterOrderFields.COMPANY)).thenReturn(companyMO);
        when(order.getBelongsToField(OrderFields.COMPANY)).thenReturn(company);

        when(companyMO.getId()).thenReturn(masterCompanyId);
        when(company.getId()).thenReturn(orderCompanyId);

        // when
        boolean result = orderValidatorsMO.checkOrderFieldWithMasterOrders(dataDefinition, order);
        // then
        Assert.assertEquals(true, result);

    }
}
