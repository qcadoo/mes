/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.basic.hooks;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class ProductHooksTest {

    private ProductHooks productHooks;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, product, prod1, prod2;

    @Mock
    private SearchCriteriaBuilder searchCriteria;

    @Mock
    private SearchCriterion criterion;

    @Mock
    private SearchResult result;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productHooks = new ProductHooks();

        PowerMockito.mockStatic(SearchRestrictions.class);

        when(entity.getDataDefinition()).thenReturn(dataDefinition);
        when(product.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.find()).thenReturn(searchCriteria);

    }

    private SearchCriteriaBuilder searchProductLikeParent(final Entity product) {
        given(SearchRestrictions.belongsTo("parent", product)).willReturn(criterion);
        given(searchCriteria.add(criterion)).willReturn(searchCriteria);

        return searchCriteria;
    }

    private static EntityList mockEntityListIterator(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Test
    public void shouldReturnWhenEntityIdIsNull() throws Exception {
        // given
        when(entity.getId()).thenReturn(null);
        // when
        productHooks.clearFamilyFromProductWhenTypeIsChanged(dataDefinition, entity);
    }

    @Test
    public void shouldReturnWhenSavingEntityIsFamily() throws Exception {
        // given
        Long entityId = 1L;
        when(entity.getId()).thenReturn(entityId);
        when(entity.getStringField(ProductFields.ENTITY_TYPE)).thenReturn("02productsFamily");
        when(entity.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.get(entityId)).thenReturn(product);
        // when
        productHooks.clearFamilyFromProductWhenTypeIsChanged(dataDefinition, entity);

        // then
    }

    @Test
    public void shouldDeleteFamilyFromProductsWhenEntityTypeIsChanged() throws Exception {
        // given
        Long entityId = 1L;
        when(entity.getId()).thenReturn(entityId);
        when(entity.getStringField(ProductFields.ENTITY_TYPE)).thenReturn("01particularProduct");

        when(dataDefinition.get(entityId)).thenReturn(product);
        when(product.getStringField(ProductFields.ENTITY_TYPE)).thenReturn("02productsFamily");

        searchProductLikeParent(product);

        EntityList products = mockEntityListIterator(asList(prod1, prod2));
        when(searchCriteria.list()).thenReturn(result);
        when(result.getEntities()).thenReturn(products);

        // when
        productHooks.clearFamilyFromProductWhenTypeIsChanged(dataDefinition, entity);

        // then
        Assert.assertEquals(null, prod1.getBelongsToField("parent"));
        Assert.assertEquals(null, prod2.getBelongsToField("parent"));
    }

    @Test
    public void shouldClearExternalIdOnCopy() throws Exception {
        // given

        // when
        productHooks.clearExternalIdOnCopy(dataDefinition, entity);

        // then

        Mockito.verify(entity).setField("externalNumber", null);
    }

}
