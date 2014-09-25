package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDecimalField;
import static com.qcadoo.testing.model.EntityTestUtils.stubField;
import static com.qcadoo.testing.model.EntityTestUtils.stubId;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithCosts;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyRawInputProductComponentsCriteria;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyRawInputProductComponentsDataProvider;
import com.qcadoo.model.api.Entity;

public class OrderMaterialCostsDataGeneratorImplTest {

    private OrderMaterialsCostsDataGeneratorImpl orderMaterialsCostsDataGenerator;

    @Mock
    private OrderMaterialCostsEntityBuilder orderMaterialCostsEntityBuilder;

    @Mock
    private TechnologyRawInputProductComponentsDataProvider technologyRawInputProductComponentsDataProvider;

    @Mock
    private OrderMaterialCostsDataProvider orderMaterialCostsDataProvider;

    @Mock
    private Entity order, technology;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderMaterialsCostsDataGenerator = new OrderMaterialsCostsDataGeneratorImpl();

        ReflectionTestUtils.setField(orderMaterialsCostsDataGenerator, "orderMaterialCostsEntityBuilder",
                orderMaterialCostsEntityBuilder);
        ReflectionTestUtils.setField(orderMaterialsCostsDataGenerator, "technologyRawInputProductComponentsDataProvider",
                technologyRawInputProductComponentsDataProvider);
        ReflectionTestUtils.setField(orderMaterialsCostsDataGenerator, "orderMaterialCostsDataProvider",
                orderMaterialCostsDataProvider);

        stubMaterialCostEntityBuilder(Maps.toMap(Sets.newHashSet(1L, 2L, 3L, 4L, 5L, 6L), Functions.constant(mockEntity())));
        stubTechnologyRawProductComponents();
        stubExistingMaterialCostComponents();

        stubId(order, 101L);
        stubId(technology, 202L);
        stubBelongsToField(order, OrderFields.TECHNOLOGY, technology);
    }

    private void stubMaterialCostEntityBuilder(final Map<Long, Entity> entitiesByProductId) {
        given(orderMaterialCostsEntityBuilder.create(eq(order), any(ProductWithCosts.class))).willAnswer(new Answer<Entity>() {

            @Override
            public Entity answer(final InvocationOnMock invocation) throws Throwable {
                ProductWithCosts productWithCosts = (ProductWithCosts) invocation.getArguments()[1];
                return entitiesByProductId.get(productWithCosts.getProductId());
            }
        });
    }

    private Entity mockTechnologyInputProdCompProjection(final Long id) {
        Entity projection = mockEntity();
        stubField(projection, "id", id);
        stubDecimalField(projection, "costForNumber", BigDecimal.ONE);
        stubDecimalField(projection, "nominalCost", BigDecimal.ONE);
        stubDecimalField(projection, "lastPurchaseCost", BigDecimal.ONE);
        stubDecimalField(projection, "averageCost", BigDecimal.ONE);
        return projection;
    }

    private void stubTechnologyRawProductComponents(final Entity... techRawProdComponentProjections) {
        given(technologyRawInputProductComponentsDataProvider.findAll(any(TechnologyRawInputProductComponentsCriteria.class)))
                .willAnswer(new Answer<List<Entity>>() {

                    @Override
                    public List<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                        return Arrays.asList(techRawProdComponentProjections);
                    }
                });
    }

    private Entity mockMaterialCostComponent(final Long productId) {
        Entity materialCostComponent = mockEntity();
        Entity product = mockEntity(productId);
        stubBelongsToField(materialCostComponent, TechnologyInstOperProductInCompFields.PRODUCT, product);
        return materialCostComponent;
    }

    private void stubExistingMaterialCostComponents(final Entity... materialCostComponents) {
        given(orderMaterialCostsDataProvider.findAll(any(OrderMaterialCostsCriteria.class))).willAnswer(
                new Answer<List<Entity>>() {

                    @Override
                    public List<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                        return Arrays.asList(materialCostComponents);
                    }
                });
    }

    @Test
    public final void shouldGenerate() {
        // given
        Entity techRawProdProj1 = mockTechnologyInputProdCompProjection(1L);
        Entity techRawProdProj2 = mockTechnologyInputProdCompProjection(2L);
        stubTechnologyRawProductComponents(techRawProdProj1, techRawProdProj2);

        // there is no already existing components, e.g. order was just created.
        stubExistingMaterialCostComponents();

        Entity materialCostComponent1 = mockEntity();
        Entity materialCostComponent2 = mockEntity();
        stubMaterialCostEntityBuilder(ImmutableMap.of(1L, materialCostComponent1, 2L, materialCostComponent2));

        // when
        List<Entity> generatedMaterialCosts = orderMaterialsCostsDataGenerator.generateUpdatedMaterialsListFor(order);

        // then
        ArgumentCaptor<OrderMaterialCostsCriteria> criteriaCaptor = ArgumentCaptor.forClass(OrderMaterialCostsCriteria.class);
        verify(orderMaterialCostsDataProvider).findAll(criteriaCaptor.capture());
        OrderMaterialCostsCriteria criteria = criteriaCaptor.getValue();
        assertEquals(101L, (long) criteria.getOrderId());
        // I know, it's far from ideal solution but it's still better than mocking static SearchRestrictions ..
        assertEquals("id in (1, 2)", criteria.getProductCriteria().get().getHibernateCriterion().toString());

        assertEquals(2, generatedMaterialCosts.size());
        assertEquals(Sets.newHashSet(materialCostComponent1, materialCostComponent2), Sets.newHashSet(generatedMaterialCosts));
    }

    @Test
    public final void shouldRegenerate() {
        // given
        Entity techRawProdProj1 = mockTechnologyInputProdCompProjection(1L);
        Entity techRawProdProj2 = mockTechnologyInputProdCompProjection(2L);
        stubTechnologyRawProductComponents(techRawProdProj1, techRawProdProj2);

        Entity existingMaterialCostComponent2 = mockMaterialCostComponent(2L);
        stubExistingMaterialCostComponents(existingMaterialCostComponent2);

        Entity newlyGeneratedMaterialCostComponent = mockEntity();
        stubMaterialCostEntityBuilder(ImmutableMap.of(1L, newlyGeneratedMaterialCostComponent));

        // when
        List<Entity> generatedMaterialCosts = orderMaterialsCostsDataGenerator.generateUpdatedMaterialsListFor(order);

        // then
        ArgumentCaptor<OrderMaterialCostsCriteria> criteriaCaptor = ArgumentCaptor.forClass(OrderMaterialCostsCriteria.class);
        verify(orderMaterialCostsDataProvider).findAll(criteriaCaptor.capture());
        OrderMaterialCostsCriteria criteria = criteriaCaptor.getValue();
        assertEquals(101L, (long) criteria.getOrderId());
        // I know, it's far from ideal solution but it's still better than mocking static SearchRestrictions ..
        assertEquals("id in (1, 2)", criteria.getProductCriteria().get().getHibernateCriterion().toString());

        assertEquals(2, generatedMaterialCosts.size());
        assertEquals(Sets.newHashSet(existingMaterialCostComponent2, newlyGeneratedMaterialCostComponent),
                Sets.newHashSet(generatedMaterialCosts));
    }

    @Test
    public final void shouldRegenerateEmpty() {
        // given

        // there is no technology raw input products, e.g. all technology operations or input products have been deleted
        stubTechnologyRawProductComponents();

        // we're stubbing results of a query with in() search restriction applied, thus this results have to be empty whenever
        // technology raw product components query result is stubbed to empty list.
        stubExistingMaterialCostComponents();

        // when
        List<Entity> generatedMaterialCosts = orderMaterialsCostsDataGenerator.generateUpdatedMaterialsListFor(order);

        // then
        ArgumentCaptor<OrderMaterialCostsCriteria> criteriaCaptor = ArgumentCaptor.forClass(OrderMaterialCostsCriteria.class);
        verify(orderMaterialCostsDataProvider).findAll(criteriaCaptor.capture());
        OrderMaterialCostsCriteria criteria = criteriaCaptor.getValue();
        assertEquals(101L, (long) criteria.getOrderId());
        // I know, it's far from ideal solution but it's still better than mocking static SearchRestrictions ..
        assertEquals("id in ()", criteria.getProductCriteria().get().getHibernateCriterion().toString());

        assertTrue(generatedMaterialCosts.isEmpty());
    }

    @Test
    public final void shouldDoNothingIfTechnologyDoesNotHaveId() {
        // given
        stubId(technology, null);

        // when
        List<Entity> generatedMaterialCosts = orderMaterialsCostsDataGenerator.generateUpdatedMaterialsListFor(order);

        // then
        assertTrue(generatedMaterialCosts.isEmpty());
        verifyZeroInteractions(orderMaterialCostsDataProvider);
        verifyZeroInteractions(orderMaterialCostsEntityBuilder);
        verifyZeroInteractions(technologyRawInputProductComponentsDataProvider);
    }

    @Test
    public final void shouldNotQueryForExistingMaterialCostsIfOrderHasNotBeenPersistedYet() {
        // given
        stubId(order, null);

        Entity techRawProdProj1 = mockTechnologyInputProdCompProjection(1L);
        Entity techRawProdProj2 = mockTechnologyInputProdCompProjection(2L);
        stubTechnologyRawProductComponents(techRawProdProj1, techRawProdProj2);

        Entity materialCostComponent1 = mockEntity();
        Entity materialCostComponent2 = mockEntity();
        stubMaterialCostEntityBuilder(ImmutableMap.of(1L, materialCostComponent1, 2L, materialCostComponent2));

        // when
        List<Entity> generatedMaterialCosts = orderMaterialsCostsDataGenerator.generateUpdatedMaterialsListFor(order);

        // then
        verifyZeroInteractions(orderMaterialCostsDataProvider);
        assertEquals(2, generatedMaterialCosts.size());
        assertEquals(Sets.newHashSet(materialCostComponent1, materialCostComponent2), Sets.newHashSet(generatedMaterialCosts));
    }

}
