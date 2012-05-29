/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.technologies.tree;

import static com.google.common.collect.Lists.newArrayList;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.NODE_NUMBER;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

public class TechnologyTreeValidationServiceImplTest {

    private TechnologyTreeValidationService technologyTreeValidationService;

    private static final List<EntityTreeNode> EMPTY_TREE_NODES_LIST = new ImmutableList.Builder<EntityTreeNode>().build();

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private EntityTree tree;

    private Map<String, Set<String>> resultMap;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        technologyTreeValidationService = new TechnologyTreeValidationServiceImpl();
        given(tree.isEmpty()).willReturn(false);
        resultMap = null;

        ReflectionTestUtils.setField(technologyTreeValidationService, "dataDefinitionService", dataDefinitionService);

        given(dataDefinitionService.get("basic", "product")).willReturn(dataDefinition);
    }

    @Test
    public final void shouldReturnEmptyMapForNullTree() {
        // when
        resultMap = technologyTreeValidationService.checkConsumingManyProductsFromOneSubOp(null);

        // then
        assertNotNull(resultMap);
        assertTrue(resultMap.isEmpty());
    }

    @Test
    public final void shouldReturnEmptyMapForEmptyTree() {
        // given
        given(tree.isEmpty()).willReturn(true);

        // when
        resultMap = technologyTreeValidationService.checkConsumingManyProductsFromOneSubOp(tree);

        // then
        assertNotNull(resultMap);
        assertTrue(resultMap.isEmpty());
    }

    @Test
    public final void shouldReturnNotEmptyMapIfParentOpConsumeManyOutputsFromOneSubOp() {
        // given
        Entity product1 = mockProductComponent(1L);
        Entity product2 = mockProductComponent(2L);
        Entity product3 = mockProductComponent(3L);
        Entity product4 = mockProductComponent(4L);
        Entity product5 = mockProductComponent(5L);
        Entity product6 = mockProductComponent(6L);

        EntityTreeNode node3 = mockOperationComponent("3.", newArrayList(product6), newArrayList(product3, product4, product5));
        EntityTreeNode node2 = mockOperationComponent("2.", newArrayList(product3, product4), newArrayList(product2),
                newArrayList(node3));
        EntityTreeNode node1 = mockOperationComponent("1.", newArrayList(product2), newArrayList(product1), newArrayList(node2));
        given(tree.getRoot()).willReturn(node1);

        // when
        resultMap = technologyTreeValidationService.checkConsumingManyProductsFromOneSubOp(tree);

        // then
        assertNotNull(resultMap);
        assertFalse(resultMap.isEmpty());
        assertEquals(1, resultMap.size());
        hasNodeNumbersFor(node2, node3);
    }

    @Test
    public final void shouldReturnNotEmptyMapIfSubOpsProduceTheSameOutputsWhichAreConsumed() {
        // given
        Entity product1 = mockProductComponent(1L);
        Entity product2 = mockProductComponent(2L);
        Entity product3 = mockProductComponent(3L);
        Entity product4 = mockProductComponent(4L);
        Entity product5 = mockProductComponent(5L);
        Entity product6 = mockProductComponent(6L);

        EntityTreeNode node3 = mockOperationComponent(3L, "3.", newArrayList(product5), newArrayList(product2, product3));
        EntityTreeNode node2 = mockOperationComponent(2L, "2.", newArrayList(product6), newArrayList(product2, product4));
        EntityTreeNode node1 = mockOperationComponent(1L, "1.", newArrayList(product2), newArrayList(product1),
                newArrayList(node2, node3));
        given(tree.getRoot()).willReturn(node1);

        // when
        Map<String, Set<Entity>> returnedMap = technologyTreeValidationService
                .checkConsumingTheSameProductFromManySubOperations(tree);

        // then
        assertNotNull(returnedMap);
        assertFalse(returnedMap.isEmpty());
        assertEquals(1, returnedMap.size());
        assertTrue(returnedMap.containsKey("1."));
        assertTrue(returnedMap.get("1.").contains(product2));
    }

    @Test
    public final void shouldReturnNotEmptyMapIfManyParentOpConsumesManyOutputsFromOneSubOp() {
        // given
        Entity product1 = mockProductComponent(1L);
        Entity product2 = mockProductComponent(2L);
        Entity product3 = mockProductComponent(3L);
        Entity product4 = mockProductComponent(4L);
        Entity product5 = mockProductComponent(5L);
        Entity product6 = mockProductComponent(6L);
        Entity product7 = mockProductComponent(7L);
        Entity product8 = mockProductComponent(8L);
        Entity product9 = mockProductComponent(9L);
        Entity product10 = mockProductComponent(10L);
        Entity product11 = mockProductComponent(11L);
        Entity product12 = mockProductComponent(12L);

        EntityTreeNode node1A3 = mockOperationComponent("1.A.3.", newArrayList(product11, product12), newArrayList(product10));
        EntityTreeNode node1A2 = mockOperationComponent("1.A.2.", newArrayList(product10),
                newArrayList(product7, product8, product9), newArrayList(node1A3));
        EntityTreeNode node1A1 = mockOperationComponent("1.A.1.", newArrayList(product7, product8, product9),
                newArrayList(product2, product3), newArrayList(node1A2));
        EntityTreeNode node1B1 = mockOperationComponent("1.B.1.", newArrayList(product6), newArrayList(product4, product5));
        EntityTreeNode node1 = mockOperationComponent("1.", newArrayList(product2, product3, product4, product5),
                newArrayList(product1), newArrayList(node1A1, node1B1));

        given(tree.getRoot()).willReturn(node1);

        // when
        resultMap = technologyTreeValidationService.checkConsumingManyProductsFromOneSubOp(tree);

        // then
        assertNotNull(resultMap);
        assertFalse(resultMap.isEmpty());
        assertEquals(2, resultMap.size());
        assertEquals(2, resultMap.get(node1.getStringField(NODE_NUMBER)).size());
        hasNodeNumbersFor(node1, node1A1);
        hasNodeNumbersFor(node1, node1B1);
        hasNodeNumbersFor(node1A1, node1A2);
    }

    @Test
    public final void shouldReturnEmptyMapIfParentOpNotConsumeManyOutputsFromOneSubOp() {
        // given
        Entity product1 = mockProductComponent(1L);
        Entity product2 = mockProductComponent(2L);
        Entity product3 = mockProductComponent(3L);
        Entity product4 = mockProductComponent(4L);
        Entity product5 = mockProductComponent(5L);
        Entity product6 = mockProductComponent(6L);

        EntityTreeNode node2B = mockOperationComponent("2.B.", newArrayList(product6), newArrayList(product4));
        EntityTreeNode node2A = mockOperationComponent("2.A.", newArrayList(product5), newArrayList(product3));
        EntityTreeNode node2 = mockOperationComponent("2.", newArrayList(product3, product4), newArrayList(product2),
                newArrayList(node2A, node2B));
        EntityTreeNode node1 = mockOperationComponent("1.", newArrayList(product2), newArrayList(product1), newArrayList(node2));
        given(tree.getRoot()).willReturn(node1);

        // when
        resultMap = technologyTreeValidationService.checkConsumingManyProductsFromOneSubOp(tree);

        // then
        assertNotNull(resultMap);
        assertTrue(resultMap.isEmpty());
    }

    /* MAP ASSERTION HELPERS */

    private void hasNodeNumbersFor(final Entity parentOperation, final Entity subOperation) {
        String parentOpNodeNumber = parentOperation.getStringField(NODE_NUMBER);
        String subOpNodeNumber = subOperation.getStringField(NODE_NUMBER);
        assertFalse(resultMap.get(parentOpNodeNumber).isEmpty());
        assertTrue(resultMap.get(parentOpNodeNumber).contains(subOpNodeNumber));
    }

    /* TREE MOCKING & STUBBING HELPERS */

    private EntityTreeNode mockOperationComponent(final String nodeNumber, final Collection<Entity> inputProducts,
            final Collection<Entity> outputProducts) {
        return mockOperationComponent(nodeNumber, inputProducts, outputProducts, EMPTY_TREE_NODES_LIST);
    }

    private EntityTreeNode mockOperationComponent(final Long id, final String nodeNumber, final Collection<Entity> inputProducts,
            final Collection<Entity> outputProducts) {
        return mockOperationComponent(id, nodeNumber, inputProducts, outputProducts, EMPTY_TREE_NODES_LIST);
    }

    private EntityTreeNode mockOperationComponent(final Long id, final String nodeNumber, final Collection<Entity> inputProducts,
            final Collection<Entity> outputProducts, final List<EntityTreeNode> subOperations) {
        EntityTreeNode operationComponent = mock(EntityTreeNode.class);

        given(operationComponent.getId()).willReturn(id);

        EntityList inputProductsList = mockProductComponentsList(inputProducts);
        given(operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS)).willReturn(inputProductsList);
        given(operationComponent.getField(OPERATION_PRODUCT_IN_COMPONENTS)).willReturn(inputProductsList);

        EntityList outputProductsList = mockProductComponentsList(outputProducts);
        given(operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS)).willReturn(outputProductsList);
        given(operationComponent.getField(OPERATION_PRODUCT_OUT_COMPONENTS)).willReturn(outputProductsList);

        given(operationComponent.getField(NODE_NUMBER)).willReturn(nodeNumber);
        given(operationComponent.getStringField(NODE_NUMBER)).willReturn(nodeNumber);

        given(operationComponent.getChildren()).willReturn(subOperations);

        return operationComponent;
    }

    private EntityTreeNode mockOperationComponent(final String nodeNumber, final Collection<Entity> inputProducts,
            final Collection<Entity> outputProducts, final List<EntityTreeNode> subOperations) {
        return mockOperationComponent(null, nodeNumber, inputProducts, outputProducts, subOperations);
    }

    private EntityList mockProductComponentsList(final Collection<Entity> productComponents) {
        EntityList productComponentsList = mock(EntityList.class);
        given(productComponentsList.iterator()).willReturn(productComponents.iterator());
        given(productComponentsList.isEmpty()).willReturn(productComponents.isEmpty());
        return productComponentsList;
    }

    private Entity mockProductComponent(final Long productId) {
        Entity productComponent = mock(Entity.class);
        Entity product = mock(Entity.class);
        given(product.getField("id")).willReturn(productId);
        given(product.getId()).willReturn(productId);
        given(productComponent.getField("product")).willReturn(product);
        given(productComponent.getBelongsToField("product")).willReturn(product);
        given(dataDefinition.get(productId)).willReturn(productComponent);
        return productComponent;
    }

}
