/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.workPlans.workPlansColumnExtension;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentEntityType;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

public class WorkPlansColumnFillerTest {

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_PRODUCT_NAME = "productName";

    private static final String L_PRODUCT = "product";

    private WorkPlansColumnFiller workPlansColumnFiller;

    @Mock
    private NumberService numberService;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Mock
    private Entity order, order2, product, operationProductComponent, technology, operationComponent;

    @Mock
    private DataDefinition operationProductComponentDD;

    private EntityTree mockEntityTree(List<Entity> list) {
        EntityTree entityTree = mock(EntityTree.class);
        when(entityTree.iterator()).thenReturn(list.iterator());
        return entityTree;
    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        workPlansColumnFiller = new WorkPlansColumnFiller();

        ReflectionTestUtils.setField(workPlansColumnFiller, "numberService", numberService);
        ReflectionTestUtils.setField(workPlansColumnFiller, "productQuantitiesService", productQuantitiesService);

        given(numberService.format(Mockito.any(BigDecimal.class))).willAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BigDecimal number = (BigDecimal) args[0];
                return number.setScale(5, RoundingMode.HALF_EVEN).toString();
            }
        });

        given(order.getStringField(OrderFields.NAME)).willReturn("order");
        given(order.getStringField(OrderFields.NUMBER)).willReturn("1234");
        given(order.getField(OrderFields.PLANNED_QUANTITY)).willReturn(new BigDecimal(11));
        given(order.getBelongsToField(OrderFields.PRODUCT)).willReturn(product);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);

        given(order2.getStringField(OrderFields.NAME)).willReturn("order2");
        given(order2.getBelongsToField(OrderFields.PRODUCT)).willReturn(product);

        given(product.getId()).willReturn(1L);
        given(product.getStringField(ProductFields.NAME)).willReturn("product");
        given(product.getStringField(ProductFields.NUMBER)).willReturn("123");
        given(product.getStringField(ProductFields.UNIT)).willReturn("abc");
    }

    @Test
    public void shouldReturnCorrectColumnValuesForOrdersTable() {
        // given
        List<Entity> orders = Arrays.asList(order, order2);

        // when
        Map<Entity, Map<String, String>> orderValues = workPlansColumnFiller.getOrderValues(orders);

        // then
        assertEquals(2, orderValues.size());
        assertEquals(5, orderValues.get(order).size());
        assertEquals(5, orderValues.get(order2).size());
        assertEquals("order", orderValues.get(order).get("orderName"));
        assertEquals("1234", orderValues.get(order).get("orderNumber"));
        assertEquals("product (123)", orderValues.get(order).get("productName"));
        assertEquals("11.00000 abc", orderValues.get(order).get("plannedQuantity"));
    }

    @Test
    public void shouldReturnCorrectColumnValuesForProducts() {
        // given
        List<Entity> orders = asList(order);

        EntityTree operComps = mockEntityTree(asList(operationComponent));
        EntityTree operComps2 = mockEntityTree(asList(operationComponent));
        given(technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS)).willReturn(operComps, operComps2);

        given(operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE)).willReturn(
                TechnologyOperationComponentEntityType.OPERATION.getStringValue());
        given(operationComponent.getId()).willReturn(1L);
        EntityList prodInComps = mockEntityList(asList(operationProductComponent));
        EntityList prodInComps2 = mockEntityList(asList(operationProductComponent));
        given(operationProductComponent.getBelongsToField("operationComponent")).willReturn(operationComponent);
        given(operationProductComponent.getBelongsToField(L_PRODUCT)).willReturn(product);
        given(operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)).willReturn(
                prodInComps, prodInComps2);
        EntityList prodOutComps = mockEntityList(new ArrayList<Entity>());
        given(operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS))
                .willReturn(prodOutComps);

        given(operationProductComponent.getDataDefinition()).willReturn(operationProductComponentDD);
        given(operationProductComponentDD.getName()).willReturn(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
        OperationProductComponentWithQuantityContainer quantities = new OperationProductComponentWithQuantityContainer();
        quantities.put(operationProductComponent, new BigDecimal(11));
        given(productQuantitiesService.getProductComponentQuantities(orders)).willReturn(quantities);

        // when
        Map<Entity, Map<String, String>> columnValues = workPlansColumnFiller.getValues(orders);

        // then
        assertEquals(1, columnValues.size());
        assertEquals("product (123)", columnValues.get(operationProductComponent).get(L_PRODUCT_NAME));
        assertEquals("11.00000 abc", columnValues.get(operationProductComponent).get(L_PLANNED_QUANTITY));
    }
}
