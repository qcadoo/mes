package com.qcadoo.mes.workPlans.print;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.qcadoo.mes.workPlans.print.WorkPlanProductsService.ProductType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

public class WorkPlanProductsServiceTest {

    private WorkPlanProductsService workPlanProductsService;

    private Entity order1;

    private Entity technology;

    private Entity operationComponent1, operationComponent2;

    private Entity prodInComp1, prodInComp2, prodInComp3;

    private Entity prodOutComp2, prodOutComp4;

    private List<Entity> orders;

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        workPlanProductsService = new WorkPlanProductsService();

        order1 = mock(Entity.class);
        technology = mock(Entity.class);

        orders = mock(List.class);

        Entity operation1 = mock(Entity.class);
        Entity operation2 = mock(Entity.class);

        operationComponent1 = mock(Entity.class);
        operationComponent2 = mock(Entity.class);

        prodInComp1 = mock(Entity.class);
        prodInComp2 = mock(Entity.class);
        prodInComp3 = mock(Entity.class);

        prodOutComp2 = mock(Entity.class);
        prodOutComp4 = mock(Entity.class);

        Entity product1 = mock(Entity.class);
        Entity product2 = mock(Entity.class);
        Entity product3 = mock(Entity.class);
        Entity product4 = mock(Entity.class);

        when(product1.getStringField("name")).thenReturn("product1");
        when(product2.getStringField("name")).thenReturn("product2");
        when(product3.getStringField("name")).thenReturn("product3");
        when(product4.getStringField("name")).thenReturn("product4");

        when(prodInComp1.getBelongsToField("product")).thenReturn(product1);
        when(prodInComp2.getBelongsToField("product")).thenReturn(product2);
        when(prodInComp3.getBelongsToField("product")).thenReturn(product3);

        when(prodOutComp2.getBelongsToField("product")).thenReturn(product2);
        when(prodOutComp4.getBelongsToField("product")).thenReturn(product4);

        when(prodInComp1.getField("quantity")).thenReturn(new BigDecimal(5));
        when(prodInComp2.getField("quantity")).thenReturn(new BigDecimal(2));
        when(prodInComp3.getField("quantity")).thenReturn(new BigDecimal(1));

        when(prodOutComp2.getField("quantity")).thenReturn(new BigDecimal(1));
        when(prodOutComp4.getField("quantity")).thenReturn(new BigDecimal(1));

        EntityList prodInComps1 = mock(EntityList.class);
        EntityList prodInComps2 = mock(EntityList.class);

        EntityList prodOutComps1 = mock(EntityList.class);
        EntityList prodOutComps2 = mock(EntityList.class);

        Iterator<Entity> prodInCompsIter1 = mock(Iterator.class);
        when(prodInComps1.iterator()).thenReturn(prodInCompsIter1);
        when(prodInCompsIter1.hasNext()).thenReturn(true, false);
        when(prodInCompsIter1.next()).thenReturn(prodInComp1);

        Iterator<Entity> prodInCompsIter2 = mock(Iterator.class);
        when(prodInComps2.iterator()).thenReturn(prodInCompsIter2);
        when(prodInCompsIter2.hasNext()).thenReturn(true, true, false);
        when(prodInCompsIter2.next()).thenReturn(prodInComp2, prodInComp3);

        Iterator<Entity> prodOutCompsIter1 = mock(Iterator.class);
        when(prodOutComps1.iterator()).thenReturn(prodOutCompsIter1);
        when(prodOutCompsIter1.hasNext()).thenReturn(true, false);
        when(prodOutCompsIter1.next()).thenReturn(prodOutComp2);

        Iterator<Entity> prodOutCompsIter2 = mock(Iterator.class);
        when(prodOutComps2.iterator()).thenReturn(prodOutCompsIter2);
        when(prodOutCompsIter2.hasNext()).thenReturn(true, false);
        when(prodOutCompsIter2.next()).thenReturn(prodOutComp4);

        EntityTree operationComponents = mock(EntityTree.class);

        when(order1.getBelongsToField("technology")).thenReturn(technology);
        when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);

        when(operationComponent1.getBelongsToField("operation")).thenReturn(operation1);
        when(operationComponent2.getBelongsToField("operation")).thenReturn(operation2);

        Iterator<Entity> operationComponentIterator = mock(Iterator.class);
        when(operationComponents.iterator()).thenReturn(operationComponentIterator);
        when(operationComponentIterator.hasNext()).thenReturn(true, true, false);
        when(operationComponentIterator.next()).thenReturn(operationComponent1, operationComponent2);

        when(operationComponent1.getHasManyField("operationProductInComponents")).thenReturn(prodInComps1);
        when(operationComponent2.getHasManyField("operationProductInComponents")).thenReturn(prodInComps2);

        when(operationComponent1.getHasManyField("operationProductOutComponents")).thenReturn(prodOutComps1);
        when(operationComponent2.getHasManyField("operationProductOutComponents")).thenReturn(prodOutComps2);

        when(technology.getStringField("componentQuantityAlgorithm")).thenReturn("02perTechnology");
        when(order1.getField("plannedQuantity")).thenReturn(new BigDecimal(5));

        Iterator<Entity> orderIterator = mock(Iterator.class);
        when(orders.iterator()).thenReturn(orderIterator);
        when(orderIterator.hasNext()).thenReturn(true, false);
        when(orderIterator.next()).thenReturn(order1);
    }

    @Test
    public void shouldReturnEmptyMapIfTheresNoTechnology() {
        // given
        when(order1.getBelongsToField("technology")).thenReturn(null);

        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperation = workPlanProductsService.getProductQuantities(orders,
                ProductType.IN);

        // then
        assertTrue(inProductsPerOperation.isEmpty());
    }

    @Test
    public void shouldGetInProductsForAllOperationComponents() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperation = workPlanProductsService.getProductQuantities(orders,
                ProductType.IN);

        // then
        assertTrue(inProductsPerOperation.keySet().contains(operationComponent1));
        assertTrue(inProductsPerOperation.keySet().contains(operationComponent2));
        assertEquals(2, inProductsPerOperation.keySet().size());
    }

    @Test
    public void shouldReturnCorrectInProductsForOperationComponents() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperation = workPlanProductsService.getProductQuantities(orders,
                ProductType.IN);

        // then
        Map<Entity, BigDecimal> inProductsForOperation1 = inProductsPerOperation.get(operationComponent1);
        assertTrue(inProductsForOperation1.keySet().contains(prodInComp1));
        assertEquals(1, inProductsForOperation1.keySet().size());

        Map<Entity, BigDecimal> inProductsForOperation2 = inProductsPerOperation.get(operationComponent2);
        assertTrue(inProductsForOperation2.keySet().contains(prodInComp2));
        assertTrue(inProductsForOperation2.keySet().contains(prodInComp3));
        assertEquals(2, inProductsForOperation2.keySet().size());
    }

    @Test
    public void shouldReturnCorrectOutProductsForOperationComponents() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperation = workPlanProductsService.getProductQuantities(orders,
                ProductType.OUT);

        // then
        Map<Entity, BigDecimal> outProductsForOperation1 = inProductsPerOperation.get(operationComponent1);
        assertTrue(outProductsForOperation1.keySet().contains(prodOutComp2));
        assertEquals(1, outProductsForOperation1.keySet().size());

        Map<Entity, BigDecimal> outProductsForOperation2 = inProductsPerOperation.get(operationComponent2);
        assertTrue(outProductsForOperation2.keySet().contains(prodOutComp4));
        assertEquals(1, outProductsForOperation2.keySet().size());
    }

    @Test
    public void shouldReturnInProductsWithCorrectQuantitiesUsingSimpleAlgorithm() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsQuantitiesMap = workPlanProductsService.getProductQuantities(orders,
                ProductType.IN);

        // then
        Map<Entity, BigDecimal> inProductsForOperation1quantities = inProductsQuantitiesMap.get(operationComponent1);
        Map<Entity, BigDecimal> inProductsForOperation2quantities = inProductsQuantitiesMap.get(operationComponent2);

        assertEquals(new BigDecimal(25), inProductsForOperation1quantities.get(prodInComp1));
        assertEquals(new BigDecimal(10), inProductsForOperation2quantities.get(prodInComp2));
        assertEquals(new BigDecimal(5), inProductsForOperation2quantities.get(prodInComp3));
    }

    @Test
    public void shouldReturnOutProductsWithCorrectQuantitiesUsingSimpleAlgorithm() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> outProductsQuantitiesMap = workPlanProductsService.getProductQuantities(orders,
                ProductType.OUT);

        // then
        Map<Entity, BigDecimal> outProductsForOperation1quantities = outProductsQuantitiesMap.get(operationComponent1);
        Map<Entity, BigDecimal> outProductsForOperation2quantities = outProductsQuantitiesMap.get(operationComponent2);

        assertEquals(new BigDecimal(5), outProductsForOperation1quantities.get(prodOutComp2));
        assertEquals(new BigDecimal(5), outProductsForOperation2quantities.get(prodOutComp4));
    }

    @Test
    @Ignore
    public void shouldReturnInProductsWithCorrectQuantitiesUsingDetailedAlgorithm() {
        // given
        when(technology.getStringField("componentQuantityAlgorithm")).thenReturn("01perProductOut");

        // when
        Map<Entity, Map<Entity, BigDecimal>> inProductsQuantitiesMap = workPlanProductsService.getProductQuantities(orders,
                ProductType.IN);

        // then
        Map<Entity, BigDecimal> inProductsForOperation1quantities = inProductsQuantitiesMap.get(operationComponent1);
        Map<Entity, BigDecimal> inProductsForOperation2quantities = inProductsQuantitiesMap.get(operationComponent2);

        assertEquals(new BigDecimal(50), inProductsForOperation1quantities.get(prodInComp1));
        assertEquals(new BigDecimal(10), inProductsForOperation2quantities.get(prodInComp2));
        assertEquals(new BigDecimal(5), inProductsForOperation2quantities.get(prodInComp3));
    }

    @Test
    @Ignore
    public void shouldReturnOutProductsWithCorrectQuantitiesUsingDetailedAlgorithm() {
        // given
        when(technology.getStringField("componentQuantityAlgorithm")).thenReturn("01perProductOut");

        // when
        Map<Entity, Map<Entity, BigDecimal>> outProductsQuantitiesMap = workPlanProductsService.getProductQuantities(orders,
                ProductType.OUT);

        // then
        Map<Entity, BigDecimal> outProductsForOperation1quantities = outProductsQuantitiesMap.get(operationComponent1);
        Map<Entity, BigDecimal> outProductsForOperation2quantities = outProductsQuantitiesMap.get(operationComponent2);

        assertEquals(new BigDecimal(5), outProductsForOperation1quantities.get(prodOutComp2));
        assertEquals(new BigDecimal(10), outProductsForOperation2quantities.get(prodOutComp4));
    }
}
