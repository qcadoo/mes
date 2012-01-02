package com.qcadoo.mes.workPlans.print;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

public class WorkPlanProductsServiceTest {

    private WorkPlanProductsService workPlanProductsService;

    private Entity order;

    private Entity technology;

    private Entity operationComponent1, operationComponent2;

    private Entity product1, product2, product3, product4;

    @Before
    public void init() {
        workPlanProductsService = new WorkPlanProductsService();

        order = mock(Entity.class);
        technology = mock(Entity.class);

        Entity operation1 = mock(Entity.class);
        Entity operation2 = mock(Entity.class);

        operationComponent1 = mock(Entity.class);
        operationComponent2 = mock(Entity.class);

        Entity prodInComp1 = mock(Entity.class);
        Entity prodInComp2 = mock(Entity.class);
        Entity prodInComp3 = mock(Entity.class);

        product1 = mock(Entity.class);
        product2 = mock(Entity.class);
        product3 = mock(Entity.class);
        product4 = mock(Entity.class);

        when(prodInComp1.getBelongsToField("product")).thenReturn(product1);
        when(prodInComp2.getBelongsToField("product")).thenReturn(product2);
        when(prodInComp3.getBelongsToField("product")).thenReturn(product3);

        when(prodInComp1.getField("quantity")).thenReturn(new BigDecimal(5));
        when(prodInComp2.getField("quantity")).thenReturn(new BigDecimal(2));
        when(prodInComp3.getField("quantity")).thenReturn(new BigDecimal(1));

        EntityList prodInComps1 = mock(EntityList.class);
        EntityList prodInComps2 = mock(EntityList.class);

        @SuppressWarnings("unchecked")
        Iterator<Entity> prodInCompsIter1 = mock(Iterator.class);
        when(prodInComps1.iterator()).thenReturn(prodInCompsIter1);
        when(prodInCompsIter1.hasNext()).thenReturn(true, false);
        when(prodInCompsIter1.next()).thenReturn(prodInComp1);

        @SuppressWarnings("unchecked")
        Iterator<Entity> prodInCompsIter2 = mock(Iterator.class);
        when(prodInComps2.iterator()).thenReturn(prodInCompsIter2);
        when(prodInCompsIter2.hasNext()).thenReturn(true, true, false);
        when(prodInCompsIter2.next()).thenReturn(prodInComp2, prodInComp3);

        EntityTree operationComponents = mock(EntityTree.class);

        when(order.getBelongsToField("technology")).thenReturn(technology);
        when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);

        when(operationComponent1.getBelongsToField("operation")).thenReturn(operation1);
        when(operationComponent2.getBelongsToField("operation")).thenReturn(operation2);

        @SuppressWarnings("unchecked")
        Iterator<Entity> operationComponentIterator = mock(Iterator.class);
        when(operationComponents.iterator()).thenReturn(operationComponentIterator);
        when(operationComponentIterator.hasNext()).thenReturn(true, true, false);
        when(operationComponentIterator.next()).thenReturn(operationComponent1, operationComponent2);

        when(operationComponent1.getHasManyField("operationProductInComponents")).thenReturn(prodInComps1);
        when(operationComponent2.getHasManyField("operationProductInComponents")).thenReturn(prodInComps2);

        when(technology.getStringField("componentQuantityAlgorithm")).thenReturn("01perProductOut");
        when(order.getField("plannedQuantity")).thenReturn(new BigDecimal(5));
    }

    @Test
    public void shouldReturnEmptyMapIfTheresNoTechnology() {
        // given
        when(order.getBelongsToField("technology")).thenReturn(null);

        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperation = workPlanProductsService.getInProductsPerOperation(order);

        // then
        assertTrue(inProductsPerOperation.isEmpty());
    }

    @Test
    public void shouldGetInProductsForAllOperationComponents() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperation = workPlanProductsService.getInProductsPerOperation(order);

        // then
        assertTrue(inProductsPerOperation.keySet().contains(operationComponent1));
        assertTrue(inProductsPerOperation.keySet().contains(operationComponent2));
        assertEquals(2, inProductsPerOperation.keySet().size());
    }

    @Test
    public void shouldReturnCorrectInProductsForOperationComponents() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperation = workPlanProductsService.getInProductsPerOperation(order);

        // then
        Map<Entity, BigDecimal> inProductsForOperation1 = inProductsPerOperation.get(operationComponent1);
        assertTrue(inProductsForOperation1.keySet().contains(product1));
        assertEquals(1, inProductsForOperation1.keySet().size());

        Map<Entity, BigDecimal> inProductsForOperation2 = inProductsPerOperation.get(operationComponent2);
        assertTrue(inProductsForOperation2.keySet().contains(product2));
        assertTrue(inProductsForOperation2.keySet().contains(product3));
        assertEquals(2, inProductsForOperation2.keySet().size());
    }

    @Test
    public void shouldReturnInProductsWithCorrectQuantitiesUsingSimpleAlgorithm() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperation = workPlanProductsService.getInProductsPerOperation(order);

        // then
        Map<Entity, BigDecimal> inProductsForOperation1 = inProductsPerOperation.get(operationComponent1);
        Map<Entity, BigDecimal> inProductsForOperation2 = inProductsPerOperation.get(operationComponent2);

        assertEquals(new BigDecimal(25), inProductsForOperation1.get(product1));
        assertEquals(new BigDecimal(10), inProductsForOperation2.get(product2));
        assertEquals(new BigDecimal(5), inProductsForOperation2.get(product3));
    }
}
