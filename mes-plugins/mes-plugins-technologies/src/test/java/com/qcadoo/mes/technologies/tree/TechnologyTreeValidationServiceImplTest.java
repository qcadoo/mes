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

import com.google.common.collect.ImmutableList;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

public class TechnologyTreeValidationServiceImplTest {

    private TechnologyTreeValidationService technologyTreeValidationService;

    private static final List<EntityTreeNode> EMPTY_TREE_NODES_LIST = new ImmutableList.Builder<EntityTreeNode>().build();

    @Mock
    private EntityTree tree;

    private Map<String, Set<String>> resultMap;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        technologyTreeValidationService = new TechnologyTreeValidationServiceImpl();
        given(tree.isEmpty()).willReturn(false);
        resultMap = null;
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

        EntityTreeNode node1_A_3 = mockOperationComponent("1.A.3.", newArrayList(product11, product12), newArrayList(product10));
        EntityTreeNode node1_A_2 = mockOperationComponent("1.A.2.", newArrayList(product10),
                newArrayList(product7, product8, product9), newArrayList(node1_A_3));
        EntityTreeNode node1_A_1 = mockOperationComponent("1.A.1.", newArrayList(product7, product8, product9),
                newArrayList(product2, product3), newArrayList(node1_A_2));
        EntityTreeNode node1_B_1 = mockOperationComponent("1.B.1.", newArrayList(product6), newArrayList(product4, product5));
        EntityTreeNode node1 = mockOperationComponent("1.", newArrayList(product2, product3, product4, product5),
                newArrayList(product1), newArrayList(node1_A_1, node1_B_1));

        given(tree.getRoot()).willReturn(node1);

        // when
        resultMap = technologyTreeValidationService.checkConsumingManyProductsFromOneSubOp(tree);

        // then
        assertNotNull(resultMap);
        assertFalse(resultMap.isEmpty());
        assertEquals(2, resultMap.size());
        assertEquals(2, resultMap.get(node1.getStringField(NODE_NUMBER)).size());
        hasNodeNumbersFor(node1, node1_A_1);
        hasNodeNumbersFor(node1, node1_B_1);
        hasNodeNumbersFor(node1_A_1, node1_A_2);
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

        EntityTreeNode node2_B = mockOperationComponent("2.B.", newArrayList(product6), newArrayList(product4));
        EntityTreeNode node2_A = mockOperationComponent("2.A.", newArrayList(product5), newArrayList(product3));
        EntityTreeNode node2 = mockOperationComponent("2.", newArrayList(product3, product4), newArrayList(product2),
                newArrayList(node2_A, node2_B));
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

    private EntityTreeNode mockOperationComponent(final String nodeNuber, final Collection<Entity> inputProducts,
            final Collection<Entity> outputProducts, final List<EntityTreeNode> subOperations) {
        EntityTreeNode operationComponent = mock(EntityTreeNode.class);

        EntityList inputProductsList = mockProductComponentsList(inputProducts);
        given(operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS)).willReturn(inputProductsList);
        given(operationComponent.getField(OPERATION_PRODUCT_IN_COMPONENTS)).willReturn(inputProductsList);

        EntityList outputProductsList = mockProductComponentsList(outputProducts);
        given(operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS)).willReturn(outputProductsList);
        given(operationComponent.getField(OPERATION_PRODUCT_OUT_COMPONENTS)).willReturn(outputProductsList);

        given(operationComponent.getField(NODE_NUMBER)).willReturn(nodeNuber);
        given(operationComponent.getStringField(NODE_NUMBER)).willReturn(nodeNuber);

        given(operationComponent.getChildren()).willReturn(subOperations);

        return operationComponent;
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
        return productComponent;
    }

}
