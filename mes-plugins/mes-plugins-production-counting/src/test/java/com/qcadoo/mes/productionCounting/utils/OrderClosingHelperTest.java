package com.qcadoo.mes.productionCounting.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchResult;

public class OrderClosingHelperTest {

    private static final int NUM_OF_OPERATIONS = 4;

    private static final Long PRODUCTION_RECORD_ID = 31781L;

    private OrderClosingHelper orderClosingHelper;

    @Mock
    private DataDefinition productionRecordDD;

    @Mock
    private Entity productionRecord, order;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        orderClosingHelper = new OrderClosingHelper();

        given(productionRecord.getId()).willReturn(PRODUCTION_RECORD_ID);
        given(productionRecord.getDataDefinition()).willReturn(productionRecordDD);
        given(productionRecord.getField(ProductionRecordFields.ORDER)).willReturn(order);
        given(productionRecord.getBelongsToField(ProductionRecordFields.ORDER)).willReturn(order);

        EntityList tiocs = mockEntityList(NUM_OF_OPERATIONS);
        given(order.getHasManyField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS)).willReturn(tiocs);
        given(order.getField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS)).willReturn(tiocs);
    }

    @Test
    public final void shouldOrderCanBeClosedWhenTypeIsCummulativeAndAcceptingLastRecord() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.CUMULATED);
        productionRecordIsLast();

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertTrue(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsCummulativeAndAcceptingLastRecordButAutoCloseIsNotEnabled() {
        // given
        stubTypeOfProductionRecording(TypeOfProductionRecording.CUMULATED);
        productionRecordIsLast();

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsCummulativeAndAcceptingNotLastRecord() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.CUMULATED);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndAcceptingNotLastRecord() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        stubSearchCriteriaResults(1L, 2L, 3L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndThereIsNotEnoughtLastRecords() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionRecordIsLast();
        stubSearchCriteriaResults(1L, 2L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndRecordIsNotLastAndThereIsNotEnoughtLastRecords() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        stubSearchCriteriaResults(1L, 2L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanBeClosedWhenTypeIsForEachOpAndRecordIsLastAndThereIsEnoughtLastRecords() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionRecordIsLast();
        stubSearchCriteriaResults(1L, 2L, 3L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertTrue(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndRecordIsLastAndThereIsEnoughtLastRecordsButAutoCloseIsNotEnabled() {
        // given
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionRecordIsLast();
        stubSearchCriteriaResults(1L, 2L, 3L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanBeClosedWhenTypeIsForEachOpAndRecordIsLastAndThereIsMoreThanEnoughtLastRecords() {
        // given
        orderHasEnabledAutoClose();
        productionRecordIsLast();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        stubSearchCriteriaResults(1L, 2L, 3L, 4L, 5L, 6L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertTrue(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndRecordIsLastAndThereIsMoreThanEnoughtLastRecordsButAutoCloseIsNotEnabled() {
        // given
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionRecordIsLast();
        stubSearchCriteriaResults(1L, 2L, 3L, 4L, 5L, 6L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanBeClosedWhenTypeIsForEachOpAndRecordIsLastAndIsAlreadyAcceptedButThereIsEnoughtRecords() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionRecordIsLast();
        stubSearchCriteriaResults(1L, 2L, 3L, PRODUCTION_RECORD_ID);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertTrue(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndRecordIsLastButIsAlreadyAccepted() {
        // given
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionRecordIsLast();
        stubSearchCriteriaResults(1L, 2L, PRODUCTION_RECORD_ID);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionRecord);

        // then
        assertFalse(shouldClose);
    }

    private void orderHasEnabledAutoClose() {
        given(order.getBooleanField(OrderFieldsPC.AUTO_CLOSE_ORDER)).willReturn(true);
        given(order.getField(OrderFieldsPC.AUTO_CLOSE_ORDER)).willReturn(true);
    }

    private void productionRecordIsLast() {
        given(productionRecord.getBooleanField(ProductionRecordFields.LAST_RECORD)).willReturn(true);
        given(productionRecord.getField(ProductionRecordFields.LAST_RECORD)).willReturn(true);
    }

    private void stubTypeOfProductionRecording(final TypeOfProductionRecording type) {
        String typeAsString = type.getStringValue();
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeAsString);
        given(order.getField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeAsString);
    }

    private void stubSearchCriteriaResults(final Long... ids) {
        SearchCriteriaBuilder scb = mock(SearchCriteriaBuilder.class);

        // blind mock of fluent interface
        given(scb.add(any(SearchCriterion.class))).willReturn(scb);
        given(scb.setProjection(any(SearchProjection.class))).willReturn(scb);

        List<Entity> entities = Lists.newArrayList();
        for (Long id : ids) {
            Entity entity = mock(Entity.class);
            given(entity.getField("id")).willReturn(id);
            entities.add(entity);
        }

        SearchResult result = mock(SearchResult.class);
        given(result.getEntities()).willReturn(entities);
        given(scb.list()).willReturn(result);

        given(productionRecordDD.find()).willReturn(scb);
    }

    private EntityList mockEntityList(final int size) {
        EntityList list = mock(EntityList.class);
        given(list.size()).willReturn(size);
        return list;
    }

}
