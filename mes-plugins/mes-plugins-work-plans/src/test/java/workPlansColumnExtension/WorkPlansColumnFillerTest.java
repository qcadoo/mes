/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package workPlansColumnExtension;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.workPlans.workPlansColumnExtension.WorkPlansColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

public class WorkPlansColumnFillerTest {

    private WorkPlansColumnFiller workPlansColumnFiller;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Mock
    private Entity order, order2, product, productComponent, technology, operComp;

    @Mock
    private NumberService numberService;

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

        given(numberService.format(Mockito.any(BigDecimal.class))).willAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BigDecimal number = (BigDecimal) args[0];
                return number.setScale(3, RoundingMode.HALF_EVEN).toString();
            }
        });

        ReflectionTestUtils.setField(workPlansColumnFiller, "numberService", numberService);
        ReflectionTestUtils.setField(workPlansColumnFiller, "productQuantitiesService", productQuantitiesService);

        given(order.getStringField("name")).willReturn("order");
        given(order.getStringField("number")).willReturn("1234");
        given(order.getField("plannedQuantity")).willReturn(new BigDecimal(11));
        given(order2.getStringField("name")).willReturn("order2");

        given(order.getBelongsToField("product")).willReturn(product);
        given(order2.getBelongsToField("product")).willReturn(product);

        given(product.getStringField("name")).willReturn("product");
        given(product.getStringField("number")).willReturn("123");
        given(product.getStringField("unit")).willReturn("abc");
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
        assertEquals("11.000 abc", orderValues.get(order).get("plannedQuantity"));
    }

    @Test
    public void shouldReturnCorrectColumnValuesForProducts() {
        // given
        List<Entity> orders = asList(order);
        given(order.getBelongsToField("technology")).willReturn(technology);
        EntityTree operComps = mockEntityTree(asList(operComp));
        EntityTree operComps2 = mockEntityTree(asList(operComp));
        given(technology.getTreeField("operationComponents")).willReturn(operComps, operComps2);

        given(operComp.getStringField("entityType")).willReturn("operation");
        EntityList prodInComps = mockEntityList(asList(productComponent));
        EntityList prodInComps2 = mockEntityList(asList(productComponent));
        given(productComponent.getBelongsToField("product")).willReturn(product);
        given(operComp.getHasManyField("operationProductInComponents")).willReturn(prodInComps, prodInComps2);
        EntityList prodOutComps = mockEntityList(new ArrayList<Entity>());
        given(operComp.getHasManyField("operationProductOutComponents")).willReturn(prodOutComps);

        HashMap<Entity, BigDecimal> quantities = new HashMap<Entity, BigDecimal>();
        quantities.put(productComponent, new BigDecimal(11));
        given(productQuantitiesService.getProductComponentQuantities(orders)).willReturn(quantities);

        // when
        Map<Entity, Map<String, String>> columnValues = workPlansColumnFiller.getValues(orders);

        // then
        assertEquals(1, columnValues.size());
        assertEquals("product (123)", columnValues.get(productComponent).get("productName"));
        assertEquals("11.000 abc", columnValues.get(productComponent).get("plannedQuantity"));
    }
}
