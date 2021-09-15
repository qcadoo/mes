/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubId;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
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
    private Entity order, technology, product;

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
        stubStringField(order, OrderFields.STATE, OrderState.PENDING.getStringValue());
    }

    private void stubMaterialCostEntityBuilder(final Map<Long, Entity> entitiesByProductId) {
        given(orderMaterialCostsEntityBuilder.create(eq(order), eq(product))).willAnswer((Answer<Entity>) invocation -> {
            Entity product = (Entity) invocation.getArguments()[1];
            return entitiesByProductId.get(product.getId());
        });
    }

    private void stubTechnologyRawProductComponents(final Entity... techRawProdComponentProjections) {
        given(technologyRawInputProductComponentsDataProvider.findAll(any(TechnologyRawInputProductComponentsCriteria.class)))
                .willAnswer((Answer<List<Entity>>) invocation -> Arrays.asList(techRawProdComponentProjections));
    }

    private void stubExistingMaterialCostComponents(final Entity... materialCostComponents) {
        given(orderMaterialCostsDataProvider.findAll(any(OrderMaterialCostsCriteria.class))).willAnswer(
                (Answer<List<Entity>>) invocation -> Arrays.asList(materialCostComponents));
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

}
