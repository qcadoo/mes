package com.qcadoo.mes.productionScheduling;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

public class OrderRealizationTimeServiceImplTest {

    OrderRealizationTimeServiceImpl orderRealizationTimeServiceImpl;

    @Mock
    EntityTreeNode opComp1, opComp2;

    @Mock
    Entity op1, op2;

    @Mock
    Entity order;

    @Mock
    ProductQuantitiesService productQuantitiesService;

    @Mock
    TechnologyService technologyService;

    private static EntityList mockEntityListIterator(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    private static EntityTree mockEntityTreeIterator(List<Entity> list) {
        EntityTree entityTree = mock(EntityTree.class);
        when(entityTree.iterator()).thenReturn(list.iterator());
        return entityTree;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderRealizationTimeServiceImpl = new OrderRealizationTimeServiceImpl();

        when(opComp1.getStringField("entityType")).thenReturn("operation");
        when(opComp2.getStringField("entityType")).thenReturn("operation");

        when(opComp1.getStringField("countRealized")).thenReturn("01all");
        when(opComp2.getStringField("countRealized")).thenReturn("01all");

        when(opComp1.getField("tj")).thenReturn(new Integer(1));
        when(opComp2.getField("tj")).thenReturn(new Integer(1));

        when(opComp1.getField("countMachine")).thenReturn(new BigDecimal(1));
        when(opComp2.getField("countMachine")).thenReturn(new BigDecimal(1));

        when(opComp1.getField("tpz")).thenReturn(new Integer(1));
        when(opComp2.getField("tpz")).thenReturn(new Integer(1));

        when(opComp1.getField("timeNextOperation")).thenReturn(new Integer(1));
        when(opComp2.getField("timeNextOperation")).thenReturn(new Integer(1));

        when(opComp1.getField("productionInOneCycle")).thenReturn(new BigDecimal(1));
        when(opComp2.getField("productionInOneCycle")).thenReturn(new BigDecimal(1));

        when(opComp2.getBelongsToField("parent")).thenReturn(opComp1);

        EntityList opComp1Children = mockEntityListIterator(asList((Entity) opComp2));
        when(opComp1.getHasManyField("children")).thenReturn(opComp1Children);
        EntityList opComp2Children = mockEntityListIterator(new LinkedList<Entity>());
        when(opComp2.getHasManyField("children")).thenReturn(opComp2Children);

        Map<Entity, BigDecimal> operationRuns = new HashMap<Entity, BigDecimal>();
        operationRuns.put(opComp1, new BigDecimal(1));
        operationRuns.put(opComp2, new BigDecimal(2));

        DataDefinition dd = mock(DataDefinition.class);

        when(dd.getName()).thenReturn("technologyOperationComponent");

        when(opComp1.getDataDefinition()).thenReturn(dd);
        when(opComp2.getDataDefinition()).thenReturn(dd);

        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "operationRunsField", operationRuns);
        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "productQuantitiesService", productQuantitiesService);
        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "technologyService", technologyService);

        when(technologyService.getProductCountForOperationComponent(opComp1)).thenReturn(new BigDecimal(1));
        when(technologyService.getProductCountForOperationComponent(opComp2)).thenReturn(new BigDecimal(1));
    }

    @Test
    public void shouldReturnCorrectOperationTime() {
        // given
        boolean includeTpz = true;
        BigDecimal plannedQuantity = new BigDecimal(1);

        // when
        int time = orderRealizationTimeServiceImpl.estimateRealizationTimeForOperation(opComp1, plannedQuantity, includeTpz);

        // then
        assertEquals(6, time);
    }

    @Test
    public void shouldReturnTimesForAllOperationsInAnOrder() {
        // given
        boolean includeTpz = true;
        BigDecimal plannedQuantity = new BigDecimal(1);
        DataDefinition dd = mock(DataDefinition.class);
        when(dd.getName()).thenReturn("order");
        when(order.getDataDefinition()).thenReturn(dd);
        EntityTree orderOperationComponents = mockEntityTreeIterator(asList((Entity) opComp1, (Entity) opComp2));
        when(order.getTreeField("orderOperationComponents")).thenReturn(orderOperationComponents);

        // when
        Map<Entity, Integer> operationDurations = orderRealizationTimeServiceImpl.estimateRealizationTimes(order,
                plannedQuantity, includeTpz);

        // then
        assertEquals(new Integer(6), operationDurations.get(opComp1));
        assertEquals(new Integer(3), operationDurations.get(opComp2));
    }

    @Test
    public void shouldReturnTimesForAllOperationsInATechnology() {
        // given
        Entity technology = mock(Entity.class);
        boolean includeTpz = true;
        BigDecimal plannedQuantity = new BigDecimal(1);
        DataDefinition dd = mock(DataDefinition.class);
        when(dd.getName()).thenReturn("technology");
        when(technology.getDataDefinition()).thenReturn(dd);
        EntityTree orderOperationComponents = mockEntityTreeIterator(asList((Entity) opComp1, (Entity) opComp2));
        when(technology.getTreeField("operationComponents")).thenReturn(orderOperationComponents);

        // when
        Map<Entity, Integer> operationDurations = orderRealizationTimeServiceImpl.estimateRealizationTimes(technology,
                plannedQuantity, includeTpz);

        // then
        assertEquals(new Integer(6), operationDurations.get(opComp1));
        assertEquals(new Integer(3), operationDurations.get(opComp2));
    }
}
