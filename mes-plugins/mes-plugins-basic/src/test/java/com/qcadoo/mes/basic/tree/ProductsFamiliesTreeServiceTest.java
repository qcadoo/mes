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
package com.qcadoo.mes.basic.tree;

import static com.qcadoo.mes.basic.constants.ProductFields.PRODUCT_FAMILY_CHILDRENS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class ProductsFamiliesTreeServiceTest {

    private ProductsFamiliesTreeService productsFamiliesTreeService;

    @Mock
    private Entity productDsk, productDsk1, productDsk2, productDsk3;

    @Mock
    private Entity parent1, parent2;

    @Before
    public void init() {
        productsFamiliesTreeService = new ProductsFamiliesTreeService();
        MockitoAnnotations.initMocks(this);

        when(productDsk.getId()).thenReturn(1L);
        when(productDsk1.getId()).thenReturn(2L);
        when(productDsk2.getId()).thenReturn(3L);
        when(productDsk3.getId()).thenReturn(4L);

        EntityList children = mockEntityList(asList(productDsk1, productDsk2));
        when(productDsk.getHasManyField(PRODUCT_FAMILY_CHILDRENS)).thenReturn(children);
        when(productDsk.getStringField("entityType")).thenReturn(ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue());

        EntityList childrenDsk1 = mockEntityList(asList(productDsk3));
        when(productDsk1.getHasManyField(PRODUCT_FAMILY_CHILDRENS)).thenReturn(childrenDsk1);

        EntityList childrenDsk2 = mockEntityList(new LinkedList<Entity>());
        when(productDsk2.getHasManyField(PRODUCT_FAMILY_CHILDRENS)).thenReturn(childrenDsk2);

        String number1 = "DSK";
        String number2 = "DSK-1";
        String number3 = "DSK-2";
        String number4 = "DSK-3";

        when(productDsk.getField("parent")).thenReturn(null);
        when(productDsk1.getField("parent")).thenReturn(parent1);
        when(productDsk2.getField("parent")).thenReturn(parent1);
        when(productDsk3.getField("parent")).thenReturn(parent2);

        when(productDsk.getField("number")).thenReturn(number1);
        when(productDsk1.getField("number")).thenReturn(number2);
        when(productDsk2.getField("number")).thenReturn(number3);
        when(productDsk3.getField("number")).thenReturn(number4);
    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Test
    public void shouldReturnTreeWithoutChildrenWhenDoesnotExists() throws Exception {
        // given
        EntityList emptyList = mockEntityList(new LinkedList<Entity>());
        when(productDsk.getHasManyField(PRODUCT_FAMILY_CHILDRENS)).thenReturn(emptyList);

        List<Entity> tree = productsFamiliesTreeService.getHierarchyProductsTree(productDsk);
        // then
        assertEquals(1, tree.size());
        assertEquals(productDsk, tree.get(0));
    }

    @Test
    public void shouldReturnTreeWithTwoLevel() throws Exception {
        // given
        // when
        List<Entity> tree = productsFamiliesTreeService.getHierarchyProductsTree(productDsk);
        // then
        assertEquals(4, tree.size());
        assertEquals(productDsk3, tree.get(2));
        assertEquals(productDsk2, tree.get(3));
    }

    @Test
    public void shouldReturnTreeWithOneLevel() throws Exception {
        // given
        EntityList emptyList = mockEntityList(new LinkedList<Entity>());
        when(productDsk1.getHasManyField(PRODUCT_FAMILY_CHILDRENS)).thenReturn(emptyList);
        // when
        List<Entity> tree = productsFamiliesTreeService.getHierarchyProductsTree(productDsk);
        // then
        assertEquals(3, tree.size());
        assertEquals(productDsk2, tree.get(2));
    }
}
