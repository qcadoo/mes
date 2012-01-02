package com.qcadoo.mes.workPlans;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class WorkPlanServiceTest {

    private WorkPlanService workPlanService;

    private DataDefinitionService dataDefinitionService;

    private Entity workPlan;

    private DataDefinition workPlanDD;

    private static final String TRANSLATED_STRING = "translated string";

    @Before
    public final void init() {
        workPlanService = new WorkPlanServiceImpl();

        dataDefinitionService = mock(DataDefinitionService.class);
        TranslationService translationService = mock(TranslationService.class);
        workPlan = mock(Entity.class);
        workPlanDD = mock(DataDefinition.class);

        when(dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN)).thenReturn(
                workPlanDD);

        when(translationService.translate(Mockito.anyString(), Mockito.any(Locale.class), Mockito.anyString())).thenReturn(
                TRANSLATED_STRING);

        when(workPlanDD.getName()).thenReturn(WorkPlansConstants.MODEL_WORK_PLAN);
        when(workPlanDD.getPluginIdentifier()).thenReturn(WorkPlansConstants.PLUGIN_IDENTIFIER);
        when(workPlanDD.get(Mockito.anyLong())).thenReturn(workPlan);

        when(workPlan.getDataDefinition()).thenReturn(workPlanDD);
        when(workPlan.getId()).thenReturn(1L);

        ReflectionTestUtils.setField(workPlanService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(workPlanService, "translationService", translationService);
    }

    @Test
    public final void shouldReturnWorkPlan() throws Exception {
        // when
        Entity resultEntity = workPlanService.getWorkPlan(1L);

        // then
        Assert.assertSame(workPlanDD, resultEntity.getDataDefinition());
        Assert.assertEquals(workPlan.getId(), resultEntity.getId());

    }

    @Test
    public final void shouldGenerateWorkPlanEntity() throws Exception {
        // given
        Entity emptyWorkPlan = mock(Entity.class);
        when(workPlanDD.create()).thenReturn(emptyWorkPlan);
        when(workPlanDD.save(emptyWorkPlan)).thenReturn(emptyWorkPlan);
        when(emptyWorkPlan.getDataDefinition()).thenReturn(workPlanDD);

        Entity order = mock(Entity.class);

        @SuppressWarnings("unchecked")
        Iterator<Entity> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, true, false);
        when(iterator.next()).thenReturn(order);

        @SuppressWarnings("unchecked")
        List<Entity> orders = mock(List.class);
        when(orders.iterator()).thenReturn(iterator);
        when(orders.size()).thenReturn(3);
        when(orders.get(Mockito.anyInt())).thenReturn(order);

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> listArgCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> stringArgCaptor = ArgumentCaptor.forClass(String.class);

        // when
        workPlanService.generateWorkPlanEntity(orders);

        // then
        verify(emptyWorkPlan, times(1)).setField(Mockito.eq("orders"), listArgCaptor.capture());
        @SuppressWarnings("unchecked")
        List<Entity> resultOrders = listArgCaptor.getValue();
        Assert.assertEquals(orders.size(), resultOrders.size());
        Assert.assertSame(order, resultOrders.get(0));
        Assert.assertSame(order, resultOrders.get(1));
        Assert.assertSame(order, resultOrders.get(2));

        verify(emptyWorkPlan, times(1)).setField(Mockito.eq("name"), stringArgCaptor.capture());
        Assert.assertEquals(TRANSLATED_STRING, stringArgCaptor.getValue());

        verify(emptyWorkPlan, times(1)).setField(Mockito.eq("type"), stringArgCaptor.capture());
        Assert.assertEquals(WorkPlanType.ALL_OPERATIONS.getStringValue(), stringArgCaptor.getValue());

    }

    @Test
    public final void shouldReturnOrdersById() throws Exception {
        // given
        Entity order1 = mock(Entity.class);
        when(order1.getId()).thenReturn(1L);

        Entity order2 = mock(Entity.class);
        when(order2.getId()).thenReturn(2L);

        Entity order3 = mock(Entity.class);
        when(order3.getId()).thenReturn(3L);

        @SuppressWarnings("unchecked")
        Iterator<Long> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, true, true, false);
        when(iterator.next()).thenReturn(1L, 2L, 3L, 4L);

        @SuppressWarnings("unchecked")
        Set<Long> selectedOrderIds = mock(Set.class);
        when(selectedOrderIds.iterator()).thenReturn(iterator);
        when(selectedOrderIds.size()).thenReturn(4);

        OrderService orderService = mock(OrderService.class);
        when(orderService.getOrder(1L)).thenReturn(order1);
        when(orderService.getOrder(2L)).thenReturn(order2);
        when(orderService.getOrder(3L)).thenReturn(order3);

        ReflectionTestUtils.setField(workPlanService, "orderService", orderService);

        // when
        List<Entity> resultList = workPlanService.getSelectedOrders(selectedOrderIds);

        // then
        Assert.assertEquals(3, resultList.size());

        Assert.assertNotNull(resultList.get(0));
        Assert.assertSame(1L, resultList.get(0).getId());

        Assert.assertNotNull(resultList.get(1));
        Assert.assertSame(2L, resultList.get(1).getId());

        Assert.assertNotNull(resultList.get(2));
        Assert.assertSame(3L, resultList.get(2).getId());
    }

}
