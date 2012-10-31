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
package com.qcadoo.mes.columnExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchResult;

public class ColumnExtensionServiceImplTest {

    private ColumnExtensionService columnExtensionService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition entityDD;

    @Mock
    private Entity entity;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> entities;

    @Mock
    private Map<String, String> columnAttributes;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        columnExtensionService = new ColumnExtensionServiceImpl();

        ReflectionTestUtils.setField(columnExtensionService, "dataDefinitionService", dataDefinitionService);

    }

    private static List<Entity> mockEntityList(final List<Entity> entities) {
        @SuppressWarnings("unchecked")
        final List<Entity> entityList = mock(List.class);

        given(entityList.iterator()).willAnswer(new Answer<Iterator<Entity>>() {

            @Override
            public Iterator<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(entities).iterator();
            }
        });

        given(entityList.isEmpty()).willReturn(entities.isEmpty());

        return entityList;
    }

    @Test
    public void shouldAddColumn() {
        // given
        String pluginIdentifier = "plugin";
        String model = "model";

        given(dataDefinitionService.get(pluginIdentifier, model)).willReturn(entityDD);
        given(entityDD.create()).willReturn(entity);

        given(entity.getDataDefinition()).willReturn(entityDD);
        given(entityDD.save(entity)).willReturn(entity);

        given(entity.isValid()).willReturn(true);

        // when
        Entity result = columnExtensionService.addColumn(pluginIdentifier, model, columnAttributes);

        // then
        Assert.assertSame(entity, result);
    }

    @Test
    public void shouldDeleteColumn() {
        // given
        String pluginIdentifier = "plugin";
        String model = "model";

        entities = mockEntityList(Arrays.asList(entity));

        given(dataDefinitionService.get(pluginIdentifier, model)).willReturn(entityDD);
        given(entityDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(entities);

        given(entity.getDataDefinition()).willReturn(entityDD);

        // when
        columnExtensionService.deleteColumn(pluginIdentifier, model, columnAttributes);

        // then
        verify(entityDD).delete(entity.getId());
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsEmpty() {
        // given
        String pluginIdentifier = "plugin";
        String model = "model";

        given(dataDefinitionService.get(pluginIdentifier, model)).willReturn(entityDD);
        given(entityDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(0);

        // when
        boolean result = columnExtensionService.isColumnsEmpty(pluginIdentifier, model);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsEmpty() {
        // given
        String pluginIdentifier = "plugin";
        String model = "model";

        given(dataDefinitionService.get(pluginIdentifier, model)).willReturn(entityDD);
        given(entityDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(1);

        // when
        boolean result = columnExtensionService.isColumnsEmpty(pluginIdentifier, model);

        // then
        Assert.assertFalse(result);
    }

}
