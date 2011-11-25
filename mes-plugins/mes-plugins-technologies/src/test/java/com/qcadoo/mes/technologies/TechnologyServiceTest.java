/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.internal.api.DataAccessService;

public class TechnologyServiceTest {

    @Autowired
    private TechnologyService technologyService;

    private SearchResult result;

    private SearchCriteriaBuilder search;

    @Before
    public void init() {
        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        ReflectionTestUtils.setField(technologyService, "dataDefinitionService", dataDefinitionService);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        when(dataDefinitionService.get(Mockito.anyString(), Mockito.anyString())).thenReturn(dataDefinition);

        search = mock(SearchCriteriaBuilder.class);
        when(dataDefinition.find()).thenReturn(search);

        result = mock(SearchResult.class);
        when(search.list()).thenReturn(result);

        DataAccessService dataAccessService = mock(DataAccessService.class);
        given(dataAccessService.convertToDatabaseEntity(Mockito.any(Entity.class))).willReturn(new Object());

        SearchRestrictions searchRestrictions = new SearchRestrictions();
        ReflectionTestUtils.setField(searchRestrictions, "dataAccessService", dataAccessService);
    }

    @Ignore("not ready yet")
    @Test
    public void shouldReturnUnrelatedIfProductAndTechnologyAreUnrelated() {
        // given
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);
        when(product.getId()).thenReturn(10L);
        List<Entity> list = new LinkedList<Entity>();
        when(result.getEntities()).thenReturn(list);

        // when
        String type = technologyService.getProductType(product, technology);

        // then
        assertEquals("00unrelated", type);
    }

    @Ignore("not ready yet")
    @Test
    public void shouldReturnIntermediateIfProductIsBothInAndOut() {
        // given
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);
        when(product.getId()).thenReturn(10L);
        List<Entity> list = new LinkedList<Entity>();
        Entity productComponent = mock(Entity.class);
        when(productComponent.getBelongsToField("product")).thenReturn(product);
        SearchCriteriaBuilder otherSearch = mock(SearchCriteriaBuilder.class);
        when(search.add(SearchRestrictions.isNull("operationComponent.parent"))).thenReturn(otherSearch);
        list.add(productComponent);
        when(otherSearch.list().getEntities()).thenReturn(list);

        // when
        String type = technologyService.getProductType(product, technology);

        // then
        assertEquals("02intermediate", type);
    }
}
