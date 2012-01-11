package com.qcadoo.mes.workPlans.print;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

public class WorkPlanProductsServiceTest {

    private WorkPlanProductsService workPlanProductsService;

    @Mock
    private EntityList orders;

    @Mock
    private Entity order;

    @Mock
    private Entity technology;

    @Mock
    private EntityTree tree;

    @Mock
    private Entity product1, product2, product3, product4;

    @Mock
    private Entity productInComponent1, productInComponent2, productInComponent3;

    @Mock
    private Entity productOutComponent2, productOutComponent4;

    @Mock
    private EntityTreeNode operationComponent1, operationComponent2;

    @Mock
    private EntityList productInComponentsForOperation1;

    @Mock
    private EntityList productOutComponentsForOperation1;

    @Mock
    private EntityList productInComponentsForOperation2;

    @Mock
    private EntityList productOutComponentsForOperation2;

    private Map<Entity, List<Entity>> productInComponents;

    private Map<Entity, List<Entity>> productOutComponents;

    private BigDecimal plannedQty;

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        MockitoAnnotations.initMocks(this);

        workPlanProductsService = new WorkPlanProductsService();

        Iterator<Entity> ordersIterator = mock(Iterator.class);
        when(orders.iterator()).thenReturn(ordersIterator);
        when(ordersIterator.hasNext()).thenReturn(true, false);
        when(ordersIterator.next()).thenReturn(order);

        when(order.getBelongsToField("technology")).thenReturn(technology);

        Iterator<Entity> treeIterator = mock(Iterator.class);
        when(tree.iterator()).thenReturn(treeIterator);
        when(treeIterator.hasNext()).thenReturn(true, true, false, true, true, false, true, true, false, true, true, false);
        when(treeIterator.next()).thenReturn(operationComponent1, operationComponent2, operationComponent1, operationComponent2,
                operationComponent1, operationComponent2, operationComponent1, operationComponent2);

        productInComponentsForOperation1 = mock(EntityList.class);
        productOutComponentsForOperation1 = mock(EntityList.class);

        Iterator<Entity> productInComponentsForOperation1iterator = mock(Iterator.class);
        Iterator<Entity> productOutComponentsForOperation1iterator = mock(Iterator.class);
        when(productInComponentsForOperation1.iterator()).thenReturn(productInComponentsForOperation1iterator);
        when(productOutComponentsForOperation1.iterator()).thenReturn(productOutComponentsForOperation1iterator);
        when(productInComponentsForOperation1iterator.hasNext()).thenReturn(true, false);
        when(productOutComponentsForOperation1iterator.hasNext()).thenReturn(true, false);
        when(productInComponentsForOperation1iterator.next()).thenReturn(productInComponent1);
        when(productOutComponentsForOperation1iterator.next()).thenReturn(productOutComponent2);

        productInComponentsForOperation2 = mock(EntityList.class);
        productOutComponentsForOperation2 = mock(EntityList.class);

        Iterator<Entity> productInComponentsForOperation2iterator = mock(Iterator.class);
        Iterator<Entity> productOutComponentsForOperation2iterator = mock(Iterator.class);
        when(productInComponentsForOperation2.iterator()).thenReturn(productInComponentsForOperation2iterator);
        when(productOutComponentsForOperation2.iterator()).thenReturn(productOutComponentsForOperation2iterator);
        when(productInComponentsForOperation2iterator.hasNext()).thenReturn(true, true, false);
        when(productOutComponentsForOperation2iterator.hasNext()).thenReturn(true, false);
        when(productInComponentsForOperation2iterator.next()).thenReturn(productInComponent2, productInComponent3);
        when(productOutComponentsForOperation2iterator.next()).thenReturn(productOutComponent4);

        when(operationComponent1.getHasManyField("operationProductInComponents")).thenReturn(productInComponentsForOperation1);
        when(operationComponent1.getHasManyField("operationProductOutComponents")).thenReturn(productOutComponentsForOperation1);

        when(operationComponent2.getHasManyField("operationProductInComponents")).thenReturn(productInComponentsForOperation2);
        when(operationComponent2.getHasManyField("operationProductOutComponents")).thenReturn(productOutComponentsForOperation2);

        plannedQty = new BigDecimal(5);

        when(order.getField("plannedQuantity")).thenReturn(plannedQty);

        when(productInComponent1.getField("quantity")).thenReturn(new BigDecimal(5));
        when(productInComponent2.getField("quantity")).thenReturn(new BigDecimal(2), new BigDecimal(10));
        when(productInComponent3.getField("quantity")).thenReturn(new BigDecimal(1), new BigDecimal(5));
        when(productOutComponent2.getField("quantity")).thenReturn(new BigDecimal(1));
        when(productOutComponent4.getField("quantity")).thenReturn(new BigDecimal(1));

        productInComponents = new HashMap<Entity, List<Entity>>();
        productOutComponents = new HashMap<Entity, List<Entity>>();

        List<Entity> productInComponentsForOperation1 = new LinkedList<Entity>();
        List<Entity> productInComponentsForOperation2 = new LinkedList<Entity>();
        List<Entity> productOutComponentsForOperation1 = new LinkedList<Entity>();
        List<Entity> productOutComponentsForOperation2 = new LinkedList<Entity>();

        productInComponentsForOperation1.add(productInComponent1);
        productInComponentsForOperation2.add(productInComponent2);
        productInComponentsForOperation2.add(productInComponent3);

        productOutComponentsForOperation1.add(productOutComponent2);
        productOutComponentsForOperation2.add(productOutComponent4);

        productInComponents.put(operationComponent1, productInComponentsForOperation1);
        productInComponents.put(operationComponent2, productInComponentsForOperation2);
        productOutComponents.put(operationComponent1, productOutComponentsForOperation1);
        productOutComponents.put(operationComponent2, productOutComponentsForOperation2);

        when(product1.getId()).thenReturn(1l);
        when(product2.getId()).thenReturn(2l);
        when(product3.getId()).thenReturn(3l);
        when(product4.getId()).thenReturn(4l);

        when(technology.getBelongsToField("product")).thenReturn(product4);
        when(productInComponent1.getBelongsToField("product")).thenReturn(product1);
        when(productInComponent2.getBelongsToField("product")).thenReturn(product2);
        when(productInComponent3.getBelongsToField("product")).thenReturn(product3);
        when(productOutComponent2.getBelongsToField("product")).thenReturn(product2);
        when(productOutComponent4.getBelongsToField("product")).thenReturn(product4);

        when(tree.getRoot()).thenReturn(operationComponent2);

        EntityList operationComponent1children = mock(EntityList.class);
        EntityList operationComponent2children = mock(EntityList.class);
        Iterator<Entity> children1iterator = mock(Iterator.class);
        Iterator<Entity> children2iterator = mock(Iterator.class);
        when(children1iterator.hasNext()).thenReturn(false);
        when(children2iterator.hasNext()).thenReturn(true, false);
        when(children2iterator.next()).thenReturn(operationComponent1);

        when(operationComponent1children.iterator()).thenReturn(children1iterator);
        when(operationComponent2children.iterator()).thenReturn(children2iterator);

        when(operationComponent1.getHasManyField("children")).thenReturn(operationComponent1children);
        when(operationComponent2.getHasManyField("children")).thenReturn(operationComponent2children);

        when(technology.getTreeField("operationComponents")).thenReturn(tree);
    }

    @Test
    public void shouldReturnEmptyMapIfTheresNoTechnology() {
        // given
        when(order.getBelongsToField("technology")).thenReturn(null);

        // when
        Map<Entity, Map<Entity, BigDecimal>> productQuantitiesPerOperation = workPlanProductsService.getProductQuantities(orders);

        // then
        assertTrue(productQuantitiesPerOperation.isEmpty());
    }

    @Test
    public void shouldPreloadProductComponents() {
        // given

        // when
        Map<Entity, List<Entity>> productInComponents = workPlanProductsService.preloadProductInComponents(tree);
        Map<Entity, List<Entity>> productOutComponents = workPlanProductsService.preloadProductOutComponents(tree);

        // then
        assertEquals(productInComponentsForOperation1, productInComponents.get(operationComponent1));
        assertEquals(productInComponentsForOperation2, productInComponents.get(operationComponent2));

        assertEquals(productOutComponentsForOperation1, productOutComponents.get(operationComponent1));
        assertEquals(productOutComponentsForOperation2, productOutComponents.get(operationComponent2));
    }

    @Test
    public void shouldAlterTreeForSimpleAlgorithmCorrectly() {
        // when
        workPlanProductsService.alterTreeForSimpleAlgorithm(tree, productInComponents, productOutComponents, plannedQty);

        // then
        verify(productInComponent1).setField("quantity", new BigDecimal(25));
        verify(productInComponent2).setField("quantity", new BigDecimal(10));
        verify(productInComponent3).setField("quantity", new BigDecimal(5));
        verify(productOutComponent2).setField("quantity", new BigDecimal(5));
        verify(productOutComponent4).setField("quantity", new BigDecimal(5));
    }

    @Test
    public void shouldAlterTreeForDetailedAlgorithmCorrectly() {
        // when
        workPlanProductsService.alterTreeForDetailedAlgorithm(tree, productInComponents, productOutComponents, technology,
                plannedQty);

        // then
        verify(productOutComponent4).setField("quantity", new BigDecimal(5));
        verify(productInComponent2).setField("quantity", new BigDecimal(10));
        verify(productInComponent3).setField("quantity", new BigDecimal(5));
        verify(productOutComponent2).setField("quantity", new BigDecimal(10));
        verify(productInComponent1).setField("quantity", new BigDecimal(50));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionIfThereIsNoAlgorithmSelectedInT() {
        // when
        Map<Entity, Map<Entity, BigDecimal>> productQuantities = workPlanProductsService.getProductQuantities(orders);
    }

    @Test
    public void shouldPutCorrectQuantitiesInTheMap() {
        // given
        when(technology.getStringField("componentQuantityAlgorithm")).thenReturn("02perTechnology");

        Map<Entity, Map<Entity, BigDecimal>> productQuantitiesPerOperation = new HashMap<Entity, Map<Entity, BigDecimal>>();

        // when
        workPlanProductsService.getQuantities(productQuantitiesPerOperation, tree, productInComponents, productOutComponents,
                plannedQty);

        // then
        assertEquals(new BigDecimal(1), productQuantitiesPerOperation.get(operationComponent2).get(productOutComponent4));
        assertEquals(new BigDecimal(2), productQuantitiesPerOperation.get(operationComponent2).get(productInComponent2));
        assertEquals(new BigDecimal(1), productQuantitiesPerOperation.get(operationComponent2).get(productInComponent3));
        assertEquals(new BigDecimal(1), productQuantitiesPerOperation.get(operationComponent1).get(productOutComponent2));
        assertEquals(new BigDecimal(5), productQuantitiesPerOperation.get(operationComponent1).get(productInComponent1));
    }
}
