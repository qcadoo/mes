/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologies;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;

public class ProductQuantitiesServiceImplTest {

    private ProductQuantitiesService productQuantitiesService;

    @Mock
    private Entity order;

    @Mock
    private Entity technology;

    @Mock
    private Entity product1, product2, product3, product4;

    @Mock
    private Entity productInComponent1, productInComponent2, productInComponent3;

    @Mock
    private Entity productOutComponent2, productOutComponent4;

    @Mock
    private DataDefinition ddIn, ddOut;

    @Mock
    private EntityTreeNode operationComponent1, operationComponent2;

    @Mock
    private NumberService numberService;

    private EntityList orders;

    private EntityTree tree;

    private Map<Entity, List<Entity>> productInComponents;

    private Map<Entity, List<Entity>> productOutComponents;

    private BigDecimal plannedQty;

    private MathContext mathContext;

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

        productQuantitiesService = new ProductQuantitiesServiceImpl();

        ReflectionTestUtils.setField(productQuantitiesService, "numberService", numberService);

        orders = mockEntityListIterator(asList(order));

        when(order.getBelongsToField("technology")).thenReturn(technology);

        tree = mockEntityTreeIterator(asList((Entity) operationComponent1, (Entity) operationComponent2));

        EntityList opComp1InComp = mockEntityListIterator(asList(productInComponent1));
        EntityList opComp1InComp1 = mockEntityListIterator(asList(productInComponent1));
        EntityList opComp1InComp2 = mockEntityListIterator(asList(productInComponent1));

        EntityList opComp2InComp = mockEntityListIterator(asList(productInComponent2, productInComponent3));
        EntityList opComp2InComp1 = mockEntityListIterator(asList(productInComponent2, productInComponent3));
        EntityList opComp2InComp2 = mockEntityListIterator(asList(productInComponent2, productInComponent3));

        EntityList opComp1OutComp = mockEntityListIterator(asList(productOutComponent2));
        EntityList opComp1OutComp1 = mockEntityListIterator(asList(productOutComponent2));
        EntityList opComp1OutComp2 = mockEntityListIterator(asList(productOutComponent2));

        EntityList opComp2OutComp = mockEntityListIterator(asList(productOutComponent4));
        EntityList opComp2OutComp1 = mockEntityListIterator(asList(productOutComponent4));
        EntityList opComp2OutComp2 = mockEntityListIterator(asList(productOutComponent4));

        when(operationComponent1.getHasManyField("operationProductInComponents")).thenReturn(opComp1InComp, opComp1InComp1,
                opComp1InComp2);
        when(operationComponent1.getHasManyField("operationProductOutComponents")).thenReturn(opComp1OutComp, opComp1OutComp1,
                opComp1OutComp2);
        when(operationComponent2.getHasManyField("operationProductInComponents")).thenReturn(opComp2InComp, opComp2InComp1,
                opComp2InComp2);
        when(operationComponent2.getHasManyField("operationProductOutComponents")).thenReturn(opComp2OutComp, opComp2OutComp1,
                opComp2OutComp2);

        plannedQty = new BigDecimal(4.5f);

        when(order.getField("plannedQuantity")).thenReturn(plannedQty);

        when(productInComponent1.getField("quantity")).thenReturn(new BigDecimal(5));
        when(productInComponent2.getField("quantity")).thenReturn(new BigDecimal(2));
        when(productInComponent3.getField("quantity")).thenReturn(new BigDecimal(1));
        when(productOutComponent2.getField("quantity")).thenReturn(new BigDecimal(1));
        when(productOutComponent4.getField("quantity")).thenReturn(new BigDecimal(1));

        productInComponents = new HashMap<Entity, List<Entity>>();
        productOutComponents = new HashMap<Entity, List<Entity>>();

        productInComponents.put(operationComponent1, asList(productInComponent1));
        productInComponents.put(operationComponent2, asList(productInComponent2, productInComponent3));
        productOutComponents.put(operationComponent1, asList(productOutComponent2));
        productOutComponents.put(operationComponent2, asList(productOutComponent4));

        when(product1.getId()).thenReturn(1L);
        when(product2.getId()).thenReturn(2L);
        when(product3.getId()).thenReturn(3L);
        when(product4.getId()).thenReturn(4L);

        when(technology.getBelongsToField("product")).thenReturn(product4);
        when(productInComponent1.getBelongsToField("product")).thenReturn(product1);
        when(productInComponent2.getBelongsToField("product")).thenReturn(product2);
        when(productInComponent3.getBelongsToField("product")).thenReturn(product3);
        when(productOutComponent2.getBelongsToField("product")).thenReturn(product2);
        when(productOutComponent4.getBelongsToField("product")).thenReturn(product4);

        when(tree.getRoot()).thenReturn(operationComponent2);

        EntityList operationComponent1children = mockEntityListIterator(new LinkedList<Entity>());
        EntityList operationComponent2children = mockEntityListIterator(asList((Entity) operationComponent1));

        when(operationComponent1.getHasManyField("children")).thenReturn(operationComponent1children);
        when(operationComponent2.getHasManyField("children")).thenReturn(operationComponent2children);

        when(technology.getTreeField("operationComponents")).thenReturn(tree);

        when(ddIn.getName()).thenReturn("operationProductInComponent");
        when(ddOut.getName()).thenReturn("operationProductOutComponent");

        when(productInComponent1.getDataDefinition()).thenReturn(ddIn);
        when(productInComponent2.getDataDefinition()).thenReturn(ddIn);
        when(productInComponent3.getDataDefinition()).thenReturn(ddIn);
        when(productOutComponent2.getDataDefinition()).thenReturn(ddOut);
        when(productOutComponent4.getDataDefinition()).thenReturn(ddOut);

        mathContext = MathContext.DECIMAL64;
        when(numberService.getMathContext()).thenReturn(mathContext);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldReturnIllegalStateExceptionIfTheresNoTechnology() {
        // given
        when(order.getBelongsToField("technology")).thenReturn(null);

        // when
        productQuantitiesService.getProductComponentQuantities(orders);
    }

    @Test
    public void shouldReturnCorrectQuantities() {
        // when
        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getProductComponentQuantities(orders);

        // then
        assertEquals(new BigDecimal(50), productQuantities.get(productInComponent1));
        assertEquals(new BigDecimal(10), productQuantities.get(productInComponent2));
        assertEquals(new BigDecimal(5), productQuantities.get(productInComponent3));
        assertEquals(new BigDecimal(10), productQuantities.get(productOutComponent2));
        assertEquals(new BigDecimal(5), productQuantities.get(productOutComponent4));
    }

    @Test
    @Ignore
    public void shouldReturnCorrectQuantitiesOfInputProductsForTechnology() {
        // given

        // when
        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology, plannedQty,
                MrpAlgorithm.ALL_PRODUCTS_IN);

        // then
        assertEquals(3, productQuantities.size());
        assertEquals(new BigDecimal(50), productQuantities.get(product1));
        assertEquals(new BigDecimal(10), productQuantities.get(product2));
        assertEquals(new BigDecimal(5), productQuantities.get(product3));
    }

    @Test
    public void shouldReturnQuantitiesOfInputProductsForOrdersAndIfToldCountOnlyComponents() {
        // given
        // when
        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(orders,
                MrpAlgorithm.ONLY_COMPONENTS);

        // then
        assertEquals(2, productQuantities.size());
        assertEquals(new BigDecimal(50), productQuantities.get(product1));
        assertEquals(new BigDecimal(5), productQuantities.get(product3));

    }

    @Test
    public void shouldReturnQuantitiesAlsoForListOfComponents() {
        // given
        Entity component = mock(Entity.class);
        when(component.getBelongsToField("order")).thenReturn(order);
        // when
        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantitiesForComponents(
                Arrays.asList(component), MrpAlgorithm.ALL_PRODUCTS_IN);

        // then
        assertEquals(3, productQuantities.size());
        assertEquals(new BigDecimal(50), productQuantities.get(product1));
        assertEquals(new BigDecimal(10), productQuantities.get(product2));
        assertEquals(new BigDecimal(5), productQuantities.get(product3));
    }

    @Test
    public void shouldReturnOperationRuns() {
        // given
        Map<Entity, BigDecimal> operationRuns = new HashMap<Entity, BigDecimal>();

        // when
        productQuantitiesService.getNeededProductQuantities(orders, MrpAlgorithm.ALL_PRODUCTS_IN, operationRuns);

        // then
        assertEquals(2, operationRuns.size());
        assertEquals(new BigDecimal(5), operationRuns.get(operationComponent2));
        assertEquals(new BigDecimal(10), operationRuns.get(operationComponent1));
    }

    @Test
    public void shouldReturnOperationRunsAlsoForComponents() {
        // given
        Map<Entity, BigDecimal> operationRuns = new HashMap<Entity, BigDecimal>();

        // when
        productQuantitiesService.getProductComponentQuantities(orders, operationRuns);

        // then
        assertEquals(2, operationRuns.size());
        assertEquals(new BigDecimal(5), operationRuns.get(operationComponent2));
        assertEquals(new BigDecimal(10), operationRuns.get(operationComponent1));
    }

    @Test
    public void shouldReturnOperationRunsAlsoForPlainTechnology() {
        // given
        Map<Entity, BigDecimal> operationRuns = new HashMap<Entity, BigDecimal>();

        // when
        productQuantitiesService.getProductComponentQuantities(technology, plannedQty, operationRuns);

        // then
        assertEquals(2, operationRuns.size());
        assertEquals(new BigDecimal(5), operationRuns.get(operationComponent2));
        assertEquals(new BigDecimal(10), operationRuns.get(operationComponent1));
    }

    @Test
    public void shouldTraverseAlsoThroughReferencedTechnologies() {
        // given
        Entity refTech = mock(Entity.class);
        Entity someOpComp = mock(Entity.class);

        EntityList child = mockEntityListIterator(asList(someOpComp));
        when(operationComponent2.getHasManyField("children")).thenReturn(child);

        when(someOpComp.getStringField("entityType")).thenReturn("referenceTechnology");
        when(someOpComp.getBelongsToField("referenceTechnology")).thenReturn(refTech);

        EntityTree refTree = mockEntityTreeIterator(asList((Entity) operationComponent1));
        when(refTree.getRoot()).thenReturn(operationComponent1);
        when(refTech.getTreeField("operationComponents")).thenReturn(refTree);

        // when
        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getProductComponentQuantities(orders);

        // then
        assertEquals(new BigDecimal(50), productQuantities.get(productInComponent1));
        assertEquals(new BigDecimal(10), productQuantities.get(productInComponent2));
        assertEquals(new BigDecimal(5), productQuantities.get(productInComponent3));
        assertEquals(new BigDecimal(10), productQuantities.get(productOutComponent2));
        assertEquals(new BigDecimal(5), productQuantities.get(productOutComponent4));
    }

    @Test
    public void shouldNotRoundOperationRunsIfTjIsDivisable() {
        // given
        when(operationComponent1.getBooleanField("areProductQuantitiesDivisible")).thenReturn(true);
        when(operationComponent2.getBooleanField("areProductQuantitiesDivisible")).thenReturn(true);

        when(operationComponent1.getBooleanField("isTjDivisible")).thenReturn(true);
        when(operationComponent2.getBooleanField("isTjDivisible")).thenReturn(true);

        Map<Entity, BigDecimal> operationRuns = new HashMap<Entity, BigDecimal>();

        // when
        productQuantitiesService.getNeededProductQuantities(orders, MrpAlgorithm.ALL_PRODUCTS_IN, operationRuns);

        // then
        assertEquals(2, operationRuns.size());
        assertEquals(0, new BigDecimal(4.5).compareTo(operationRuns.get(operationComponent2)));
        assertEquals(0, new BigDecimal(9.0).compareTo(operationRuns.get(operationComponent1)));
    }

    @Test
    public void shouldNotRoundToTheIntegerOperationRunsIfOperationComponentHasDivisableProductQuantities() {
        // given
        when(operationComponent1.getBooleanField("areProductQuantitiesDivisible")).thenReturn(true);
        when(operationComponent2.getBooleanField("areProductQuantitiesDivisible")).thenReturn(true);

        // when
        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getProductComponentQuantities(orders);

        // then
        assertEquals(0, new BigDecimal(45).compareTo(productQuantities.get(productInComponent1)));
        assertEquals(0, new BigDecimal(9).compareTo(productQuantities.get(productInComponent2)));
        assertEquals(0, new BigDecimal(4.5).compareTo(productQuantities.get(productInComponent3)));
        assertEquals(0, new BigDecimal(9).compareTo(productQuantities.get(productOutComponent2)));
        assertEquals(0, new BigDecimal(4.5).compareTo(productQuantities.get(productOutComponent4)));
    }
}
