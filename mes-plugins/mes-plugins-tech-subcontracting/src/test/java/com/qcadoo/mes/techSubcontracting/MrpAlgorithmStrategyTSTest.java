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
package com.qcadoo.mes.techSubcontracting;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class MrpAlgorithmStrategyTSTest {

    private MrpAlgorithmStrategyTS algorithmStrategyTS;

    @Mock
    private Entity operComp1, operComp2;

    private Map<Entity, BigDecimal> productComponentQuantities;

    private Set<Entity> nonComponents;

    @Mock
    private DataDefinition ddIn, ddOut;

    @Mock
    private Entity product1, product2, product3, product4;

    @Mock
    private Entity productInComponent1, productInComponent2, productInComponent3;

    @Mock
    private Entity productOutComponent2, productOutComponent4;

    @Before
    public void init() {
        algorithmStrategyTS = new MrpAlgorithmStrategyTS();
        MockitoAnnotations.initMocks(this);

        EntityList opComp1InComp = mockEntityListIterator(asList(productInComponent1, productInComponent3));
        EntityList opComp1OutComp = mockEntityListIterator(asList(productOutComponent2));

        EntityList opComp2InComp = mockEntityListIterator(asList(productInComponent2));
        EntityList opComp2OutComp = mockEntityListIterator(asList(productOutComponent4));

        when(operComp1.getHasManyField("operationProductInComponents")).thenReturn(opComp1InComp);
        when(operComp1.getHasManyField("operationProductOutComponents")).thenReturn(opComp1OutComp);

        when(operComp2.getHasManyField("operationProductInComponents")).thenReturn(opComp2InComp);
        when(operComp2.getHasManyField("operationProductOutComponents")).thenReturn(opComp2OutComp);

        when(productInComponent1.getField("quantity")).thenReturn(new BigDecimal(5));
        when(productInComponent3.getField("quantity")).thenReturn(BigDecimal.ONE);
        when(productOutComponent2.getField("quantity")).thenReturn(BigDecimal.ONE);

        when(productInComponent2.getField("quantity")).thenReturn(new BigDecimal(2));
        when(productOutComponent4.getField("quantity")).thenReturn(BigDecimal.ONE);

        productComponentQuantities = new HashMap<Entity, BigDecimal>();
        nonComponents = new HashSet<Entity>();

        productComponentQuantities.put(productInComponent1, new BigDecimal(5));
        productComponentQuantities.put(productInComponent3, BigDecimal.ONE);
        productComponentQuantities.put(productOutComponent2, BigDecimal.ONE);

        productComponentQuantities.put(productInComponent2, new BigDecimal(2));
        productComponentQuantities.put(productOutComponent4, BigDecimal.ONE);

        nonComponents.add(productInComponent2);

        when(product1.getId()).thenReturn(1L);
        when(product2.getId()).thenReturn(2L);
        when(product3.getId()).thenReturn(3L);
        when(product4.getId()).thenReturn(4L);

        when(productInComponent1.getBelongsToField("product")).thenReturn(product1);
        when(productInComponent2.getBelongsToField("product")).thenReturn(product2);
        when(productInComponent3.getBelongsToField("product")).thenReturn(product3);
        when(productOutComponent2.getBelongsToField("product")).thenReturn(product2);
        when(productOutComponent4.getBelongsToField("product")).thenReturn(product4);

        when(productInComponent1.getBelongsToField("operationComponent")).thenReturn(operComp1);
        when(productInComponent3.getBelongsToField("operationComponent")).thenReturn(operComp1);
        when(productOutComponent2.getBelongsToField("operationComponent")).thenReturn(operComp1);

        when(productInComponent2.getBelongsToField("operationComponent")).thenReturn(operComp2);
        when(productOutComponent4.getBelongsToField("operationComponent")).thenReturn(operComp2);

        when(ddIn.getName()).thenReturn("productInComponent");
        when(ddOut.getName()).thenReturn("productOutComponent");

        when(productInComponent1.getDataDefinition()).thenReturn(ddIn);
        when(productInComponent2.getDataDefinition()).thenReturn(ddIn);
        when(productInComponent3.getDataDefinition()).thenReturn(ddIn);
        when(productOutComponent2.getDataDefinition()).thenReturn(ddOut);
        when(productOutComponent4.getDataDefinition()).thenReturn(ddOut);

    }

    private static EntityList mockEntityListIterator(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Test
    public void shouldReturnMapWithProductFromOneSubcontractingOperations() throws Exception {
        // given
        when(operComp1.getBooleanField("isSubcontracting")).thenReturn(true);
        // when
        Map<Entity, BigDecimal> productsMap = algorithmStrategyTS.perform(productComponentQuantities, nonComponents,
                MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS, "productInComponent");

        assertEquals(3, productsMap.size());
        assertEquals(new BigDecimal(5), productsMap.get(product1));
        assertEquals(BigDecimal.ONE, productsMap.get(product2));
        assertEquals(BigDecimal.ONE, productsMap.get(product3));
    }

    @Test
    public void shouldReturnMapWithoutProductFromSubcontractingOperation() throws Exception {
        // given
        Map<Entity, BigDecimal> productsMap = algorithmStrategyTS.perform(productComponentQuantities, nonComponents,
                MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS, "productInComponent");

        assertEquals(2, productsMap.size());
        assertEquals(new BigDecimal(5), productsMap.get(product1));
        assertEquals(BigDecimal.ONE, productsMap.get(product3));

        // then
    }

    @Test
    public void shouldReturnProductFromAllSubcontractingOperation() throws Exception {
        // given
        when(operComp1.getBooleanField("isSubcontracting")).thenReturn(true);
        when(operComp2.getBooleanField("isSubcontracting")).thenReturn(true);
        // when
        Map<Entity, BigDecimal> productsMap = algorithmStrategyTS.perform(productComponentQuantities, nonComponents,
                MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS, "productInComponent");

        // then
        assertEquals(4, productsMap.size());
        assertEquals(new BigDecimal(5), productsMap.get(product1));
        assertEquals(BigDecimal.ONE, productsMap.get(product3));
        assertEquals(BigDecimal.ONE, productsMap.get(product2));
        assertEquals(BigDecimal.ONE, productsMap.get(product4));
    }

}
