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
package com.qcadoo.mes.technologies;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

public class TechnologyServiceTest {

    TechnologyService technologyService;

    @Mock
    EntityTreeNode opComp1, opComp2;

    @Mock
    Entity product1, product2;

    @Mock
    Entity prodOutComp1, prodOutComp2;

    @Mock
    Entity prodInComp1;

    @Mock
    DataDefinition dataDefinition;

    @Mock
    Entity technology;

    private static EntityList mockEntityIterator(List<Entity> entities) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(entities.iterator());
        return entityList;
    }

    @Before
    public void init() {
        technologyService = new TechnologyService();

        MockitoAnnotations.initMocks(this);

        when(product1.getId()).thenReturn(1L);
        when(product2.getId()).thenReturn(2L);

        when(opComp1.getBelongsToField("parent")).thenReturn(null);
        when(opComp2.getBelongsToField("parent")).thenReturn(opComp1);
        EntityList opComp1Children = mockEntityIterator(asList((Entity) opComp2));
        when(opComp1.getHasManyField("children")).thenReturn(opComp1Children);

        when(prodOutComp1.getBelongsToField("product")).thenReturn(product1);
        when(prodOutComp2.getBelongsToField("product")).thenReturn(product2);
        when(prodInComp1.getBelongsToField("product")).thenReturn(product1);

        when(prodOutComp1.getField("quantity")).thenReturn(new BigDecimal(10));
        when(prodOutComp2.getField("quantity")).thenReturn(new BigDecimal(10));

        EntityList opComp1prodIns = mockEntityIterator(asList(prodInComp1));
        when(opComp1.getHasManyField("operationProductInComponents")).thenReturn(opComp1prodIns);

        EntityList opComp2prodOuts = mockEntityIterator(asList(prodOutComp1, prodOutComp2));
        when(opComp2.getHasManyField("operationProductOutComponents")).thenReturn(opComp2prodOuts);

        when(dataDefinition.getName()).thenReturn("technologyOperationComponent");

        when(opComp2.getDataDefinition()).thenReturn(dataDefinition);
        when(opComp1.getDataDefinition()).thenReturn(dataDefinition);

        when(opComp1.getBelongsToField("technology")).thenReturn(technology);
        when(opComp2.getBelongsToField("technology")).thenReturn(technology);
    }

    @Test
    public void shouldReturnOutputProductCountForOperationComponent() {
        // when
        BigDecimal count = technologyService.getProductCountForOperationComponent(opComp2);

        // then
        assertEquals(new BigDecimal(10), count);
    }

    @Test
    public void shouldThrowAnExceptionIfThereAreNoProductsOrIntermediates() {
        // given
        EntityList opComp2prodOuts = mockEntityIterator(asList(prodOutComp2));
        when(opComp2.getHasManyField("operationProductOutComponents")).thenReturn(opComp2prodOuts);

        // when
        try {
            technologyService.getProductCountForOperationComponent(opComp2);
            fail();
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void shouldReturnOutputProductCountForOperationComponentAlsoForTechnologyInstanceOperationComponent() {
        // given
        when(dataDefinition.getName()).thenReturn("technologyInstanceOperationComponent");
        when(opComp2.getBelongsToField("technologyOperationComponent")).thenReturn(opComp2);
        when(opComp1.getBelongsToField("technologyOperationComponent")).thenReturn(opComp1);

        // when
        BigDecimal count = technologyService.getProductCountForOperationComponent(opComp2);

        // then
        assertEquals(new BigDecimal(10), count);
    }

    @Test
    public void shouldReturnOutputProductCountForOperationComponentAlsoForReferenceTechnology() {
        // given
        when(opComp2.getStringField("entityType")).thenReturn("referenceTechnology");
        Entity refTech = mock(Entity.class);
        when(opComp2.getBelongsToField("referenceTechnology")).thenReturn(refTech);

        EntityTree tree = mock(EntityTree.class);
        when(refTech.getTreeField("operationComponents")).thenReturn(tree);
        when(tree.getRoot()).thenReturn(opComp2);

        // when
        BigDecimal count = technologyService.getProductCountForOperationComponent(opComp2);

        // then
        assertEquals(new BigDecimal(10), count);
    }

    @Test
    public void shouldReturnOutputProductCountForOperationComponentAlsoIfParentOperationIsNull() {
        // given
        when(opComp2.getBelongsToField("parent")).thenReturn(null);
        when(prodOutComp2.getBelongsToField("product")).thenReturn(product2);
        when(prodOutComp1.getBelongsToField("product")).thenReturn(product1);
        when(technology.getBelongsToField("product")).thenReturn(product2);

        // when
        BigDecimal count = technologyService.getProductCountForOperationComponent(opComp2);

        // then
        assertEquals(new BigDecimal(10), count);
    }
}
