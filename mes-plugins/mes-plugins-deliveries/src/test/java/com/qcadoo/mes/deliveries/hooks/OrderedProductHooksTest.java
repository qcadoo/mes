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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;
import static org.mockito.Mockito.when;

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

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class OrderedProductHooksTest {

    private OrderedProductHooks orderedProductHooks;

    @Mock
    private DataDefinition orderedProductDD;

    @Mock
    private Entity orderedProduct, delivery, product;

    @Mock
    private FieldDefinition productField;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        orderedProductHooks = new OrderedProductHooks();
        PowerMockito.mockStatic(SearchRestrictions.class);

        when(orderedProduct.getBelongsToField(DELIVERY)).thenReturn(delivery);
        when(orderedProduct.getBelongsToField(PRODUCT)).thenReturn(product);
        when(orderedProductDD.find()).thenReturn(searchCriteriaBuilder);
        Long id = 1L;
        when(orderedProduct.getId()).thenReturn(id);
        SearchCriterion criterion1 = SearchRestrictions.belongsTo(DELIVERY, delivery);
        SearchCriterion criterion2 = SearchRestrictions.belongsTo(PRODUCT, product);
        SearchCriterion criterion3 = SearchRestrictions.ne("id", id);
        when(searchCriteriaBuilder.add(criterion1)).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.add(criterion2)).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.add(criterion3)).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.setMaxResults(1)).thenReturn(searchCriteriaBuilder);
    }

    @Test
    public void shouldReturnFalseAndAddErrorForEntityWhenOrderedProductAlreadyExists() throws Exception {
        // given
        when(searchCriteriaBuilder.uniqueResult()).thenReturn(delivery);
        when(orderedProductDD.getField(PRODUCT)).thenReturn(productField);

        // when
        boolean result = orderedProductHooks.checkIfOrderedProductAlreadyExists(orderedProductDD, orderedProduct);

        // then
        Assert.assertFalse(result);

        Mockito.verify(orderedProduct).addError(productField, "deliveries.orderedProduct.error.alreadyExists");
    }

    @Test
    public void shouldReturnTrue() throws Exception {
        // given
        when(searchCriteriaBuilder.uniqueResult()).thenReturn(null);
        when(orderedProductDD.getField(PRODUCT)).thenReturn(productField);

        // when
        boolean result = orderedProductHooks.checkIfOrderedProductAlreadyExists(orderedProductDD, orderedProduct);

        // then
        Assert.assertTrue(result);
    }

}
