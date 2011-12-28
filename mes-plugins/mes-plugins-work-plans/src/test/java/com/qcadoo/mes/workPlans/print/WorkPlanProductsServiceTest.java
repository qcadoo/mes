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

import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

public class WorkPlanProductsServiceTest {

    private WorkPlanProductsService workPlanProductsService;

    private Entity order;

    private Entity technology;

    private Entity operationComponent1, operationComponent2;

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        workPlanProductsService = new WorkPlanProductsService();

        order = mock(Entity.class);
        technology = mock(Entity.class);
        operationComponent1 = mock(Entity.class);
        operationComponent2 = mock(Entity.class);

        Entity operation1 = mock(Entity.class);
        Entity operation2 = mock(Entity.class);

        Entity prodInComp1 = mock(Entity.class);
        Entity prodInComp2 = mock(Entity.class);
        Entity prodInComp3 = mock(Entity.class);

        Entity product1 = mock(Entity.class);
        Entity product2 = mock(Entity.class);
        Entity product3 = mock(Entity.class);

        when(prodInComp1.getBelongsToField("product")).thenReturn(product1);
        when(prodInComp2.getBelongsToField("product")).thenReturn(product2);
        when(prodInComp3.getBelongsToField("product")).thenReturn(product3);

        EntityList prodInComps1 = mock(EntityList.class);
        EntityList prodInComps2 = mock(EntityList.class);

        Iterator prodInCompsIter1 = mock(Iterator.class);
        when(prodInComps1.iterator()).thenReturn(prodInCompsIter1);
        when(prodInCompsIter1.hasNext()).thenReturn(true, true, false);
        when(prodInCompsIter1.next()).thenReturn(prodInComp1, prodInComp2);

        Iterator prodInCompsIter2 = mock(Iterator.class);
        when(prodInComps2.iterator()).thenReturn(prodInCompsIter1);
        when(prodInCompsIter2.hasNext()).thenReturn(true, false);
        when(prodInCompsIter2.next()).thenReturn(prodInComp3);

        EntityTree operationComponents = mock(EntityTree.class);

        when(order.getBelongsToField("technology")).thenReturn(technology);
        when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);

        when(operationComponent1.getBelongsToField("operation")).thenReturn(operation1);
        when(operationComponent2.getBelongsToField("operation")).thenReturn(operation2);

        Iterator operationComponentIterator = mock(Iterator.class);
        when(operationComponents.iterator()).thenReturn(operationComponentIterator);
        when(operationComponentIterator.hasNext()).thenReturn(true, true, false);
        when(operationComponentIterator.next()).thenReturn(operationComponent1, operationComponent2);

        when(operationComponent1.getHasManyField("operationProductInComponents")).thenReturn(prodInComps1);
        when(operationComponent2.getHasManyField("operationProductInComponents")).thenReturn(prodInComps2);
    }

    @Test
    public void shouldGetAllInProductsForOrder() {
        // given
        String type = WorkPlanType.ALL_OPERATIONS.getStringValue();

        // when
        Map<Entity, Map<Entity, BigDecimal>> inProducts = workPlanProductsService.getInProductsForOrder(order, type);

        // then
        assertTrue(inProducts.keySet().contains(operationComponent1));
        assertTrue(inProducts.keySet().contains(operationComponent2));
        assertEquals(2, inProducts.keySet().size());
    }
}
