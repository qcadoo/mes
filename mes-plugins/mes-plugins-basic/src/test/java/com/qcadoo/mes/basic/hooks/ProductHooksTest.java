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
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class ProductHooksTest {

    private ProductHooks productHooks;

    @Mock
    private DataDefinition productDD;

    @Mock
    private Entity product;

    @Mock
    private SearchCriteriaBuilder searchCriteria;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productHooks = new ProductHooks();

        PowerMockito.mockStatic(SearchRestrictions.class);

        when(product.getDataDefinition()).thenReturn(productDD);
        when(productDD.find()).thenReturn(searchCriteria);

    }

    @Test
    public void shouldClearExternalIdOnCopy() throws Exception {
        // given

        // when
        productHooks.clearExternalIdOnCopy(productDD, product);

        // then

        Mockito.verify(product).setField("externalNumber", null);
    }

}
