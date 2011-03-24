/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.view.states.components;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.grid.GridComponentColumn;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.states.AbstractStateTest;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.Restriction;
import com.qcadoo.model.api.search.RestrictionOperator;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.types.BelongsToType;
import com.qcadoo.model.api.types.HasManyType;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.ExpressionServiceImpl;
import com.qcadoo.model.internal.types.StringType;

public class GridComponentStateTest extends AbstractStateTest {

    private Entity entity;

    private ViewDefinitionState viewDefinitionState;

    private GridComponentState grid;

    private DataDefinition productDataDefinition;

    private DataDefinition substituteDataDefinition;

    private FieldDefinition substitutesFieldDefinition;

    private TranslationService translationService;

    private JSONObject json;

    private Map<String, GridComponentColumn> columns;

    private SearchCriteriaBuilder substituteCriteria;

    @SuppressWarnings("unchecked")
    @Before
    public void init() throws Exception {
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(GridComponentState.JSON_SELECTED_ENTITY_ID, 13L);
        jsonContent.put(GridComponentState.JSON_MULTISELECT_MODE, false);
        JSONObject jsonSelected = new JSONObject();
        jsonSelected.put("13", true);
        jsonContent.put(GridComponentState.JSON_SELECTED_ENTITIES, jsonSelected);
        jsonContent.put(GridComponentState.JSON_BELONGS_TO_ENTITY_ID, 1L);
        jsonContent.put(GridComponentState.JSON_FIRST_ENTITY, 60);
        jsonContent.put(GridComponentState.JSON_MAX_ENTITIES, 30);
        jsonContent.put(GridComponentState.JSON_FILTERS_ENABLED, true);

        JSONObject jsonOrder = new JSONObject(ImmutableMap.of("column", "asd", "direction", "asc"));

        jsonContent.put(GridComponentState.JSON_ORDER, jsonOrder);

        JSONObject jsonFilters = new JSONObject();
        jsonFilters.put("asd", "test");
        jsonFilters.put("qwe", "test2");

        jsonContent.put(GridComponentState.JSON_FILTERS, jsonFilters);

        json = new JSONObject(Collections.singletonMap(ComponentState.JSON_CONTENT, jsonContent));

        entity = mock(Entity.class);
        given(entity.getField("name")).willReturn("text");

        viewDefinitionState = mock(ViewDefinitionState.class);

        productDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        substituteDataDefinition = mock(DataDefinition.class);

        HasManyType substitutesFieldType = mock(HasManyType.class);
        given(substitutesFieldType.getDataDefinition()).willReturn(substituteDataDefinition);
        given(substitutesFieldType.getJoinFieldName()).willReturn("product");

        substitutesFieldDefinition = mock(FieldDefinition.class);
        given(substitutesFieldDefinition.getType()).willReturn(substitutesFieldType);
        given(substitutesFieldDefinition.getName()).willReturn("substitutes");
        given(substitutesFieldDefinition.getDataDefinition()).willReturn(substituteDataDefinition);

        substituteCriteria = mock(SearchCriteriaBuilder.class);

        given(substituteDataDefinition.getPluginIdentifier()).willReturn("plugin");
        given(substituteDataDefinition.getName()).willReturn("substitute");
        given(substituteDataDefinition.find()).willReturn(substituteCriteria);

        given(productDataDefinition.getPluginIdentifier()).willReturn("plugin");
        given(productDataDefinition.getName()).willReturn("product");
        given(productDataDefinition.getField("substitutes")).willReturn(substitutesFieldDefinition);

        columns = new LinkedHashMap<String, GridComponentColumn>();

        translationService = mock(TranslationService.class);
        given(translationService.translate(Mockito.anyString(), Mockito.any(Locale.class))).willReturn("i18n");
        given(translationService.translate(Mockito.anyList(), Mockito.any(Locale.class))).willReturn("i18n");

        grid = new GridComponentState(substitutesFieldDefinition, columns, null, null);
        grid.setDataDefinition(substituteDataDefinition);
        grid.setTranslationService(translationService);

        new ExpressionServiceImpl().init();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldInitialize() throws Exception {
        // when
        grid.initialize(json, Locale.ENGLISH);

        // then
        assertEquals(substitutesFieldDefinition, getField(grid, "belongsToFieldDefinition"));
        assertEquals(13L, getField(grid, "selectedEntityId"));
        assertEquals(1L, getField(grid, "belongsToEntityId"));
        assertNull(getField(grid, "entities"));
        assertEquals(0, getField(grid, "totalEntities"));
        assertEquals(60, getField(grid, "firstResult"));
        assertEquals(30, getField(grid, "maxResults"));
        assertTrue((Boolean) getField(grid, "filtersEnabled"));

        assertEquals("asd", getField(grid, "orderColumn"));
        assertEquals("asc", getField(grid, "orderDirection"));

        Map<String, String> filters = (Map<String, String>) getField(grid, "filters");

        assertEquals(2, filters.size());
        assertEquals("test", filters.get("asd"));
        assertEquals("test2", filters.get("qwe"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldInitializeWithoutData() throws Exception {
        // given
        grid = new GridComponentState(null, columns, null, null);
        grid.setDataDefinition(substituteDataDefinition);

        JSONObject json = new JSONObject(Collections.singletonMap(ComponentState.JSON_CONTENT, new JSONObject()));

        // when
        grid.initialize(json, Locale.ENGLISH);

        // then
        assertNull(getField(grid, "belongsToFieldDefinition"));
        assertNull(getField(grid, "selectedEntityId"));
        assertNull(getField(grid, "belongsToEntityId"));
        assertNull(getField(grid, "entities"));
        assertEquals(0, getField(grid, "totalEntities"));
        assertEquals(0, getField(grid, "firstResult"));
        assertEquals(Integer.MAX_VALUE, getField(grid, "maxResults"));
        assertTrue((Boolean) getField(grid, "filtersEnabled"));

        assertNull(getField(grid, "orderColumn"));
        assertNull(getField(grid, "orderDirection"));

        Map<String, String> filters = (Map<String, String>) getField(grid, "filters");

        assertEquals(0, filters.size());
    }

    @Test
    public void shouldRender() throws Exception {
        // given
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);

        // then
        JSONObject json = grid.render().getJSONObject(ComponentState.JSON_CONTENT);

        // then
        assertEquals(13L, json.getLong(GridComponentState.JSON_SELECTED_ENTITY_ID));
        assertEquals(1L, json.getLong(GridComponentState.JSON_BELONGS_TO_ENTITY_ID));
        assertEquals(60, json.getInt(GridComponentState.JSON_FIRST_ENTITY));
        assertEquals(30, json.getInt(GridComponentState.JSON_MAX_ENTITIES));
        assertTrue(json.getBoolean(GridComponentState.JSON_FILTERS_ENABLED));
        assertEquals("asd", json.getJSONObject(GridComponentState.JSON_ORDER).getString("column"));
        assertEquals("asc", json.getJSONObject(GridComponentState.JSON_ORDER).getString("direction"));
        assertEquals("test", json.getJSONObject(GridComponentState.JSON_FILTERS).getString("asd"));
        assertEquals("test2", json.getJSONObject(GridComponentState.JSON_FILTERS).getString("qwe"));
        assertEquals(0, json.getInt(GridComponentState.JSON_TOTAL_ENTITIES));
        assertEquals(0, json.getJSONArray(GridComponentState.JSON_ENTITIES).length());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldPaginateResults() throws Exception {
        // given
        Entity entity = mock(Entity.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(31);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList(), Lists.newArrayList(entity));
        grid.initialize(json, Locale.ENGLISH);

        // when
        JSONObject json = grid.render().getJSONObject(ComponentState.JSON_CONTENT);

        // then
        assertEquals(30, json.getInt(GridComponentState.JSON_FIRST_ENTITY));
        assertEquals(30, json.getInt(GridComponentState.JSON_MAX_ENTITIES));
        verify(substituteCriteria, times(1)).withFirstResult(60);
        verify(substituteCriteria, times(1)).withFirstResult(30);
        verify(substituteCriteria, times(2)).withMaxResults(30);
        verify(substituteCriteria, times(2)).list();
    }

    @Test
    public void shouldOrderResults() throws Exception {
        // given
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);

        FieldDefinition field = mock(FieldDefinition.class);
        given(field.getName()).willReturn("asdName");

        GridComponentColumn column = new GridComponentColumn("asd");
        column.addField(field);

        columns.put("asd", column);

        // when
        grid.render();

        // then
        verify(substituteCriteria).orderAscBy("asdName");
    }

    @Test
    public void shouldRestrictResults() throws Exception {
        // given
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);

        FieldDefinition field = mock(FieldDefinition.class);
        given(field.getName()).willReturn("asdName");

        GridComponentColumn column = new GridComponentColumn("asd");
        column.addField(field);

        columns.put("asd", column);

        // when
        grid.render();

        // then
        verify(substituteCriteria).restrictedWith(Restrictions.forOperator(RestrictionOperator.EQ, null, "test"));
    }

    @Test
    public void shouldOrderResultsUsingExpression() throws Exception {
        // given
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());

        JSONObject jsonOrder = new JSONObject(ImmutableMap.of("column", "asd", "direction", "desc"));

        json.getJSONObject(ComponentState.JSON_CONTENT).put(GridComponentState.JSON_ORDER, jsonOrder);

        grid.initialize(json, Locale.ENGLISH);

        GridComponentColumn column = new GridComponentColumn("asd");
        column.setExpression("#product['name']");

        columns.put("asd", column);

        // when
        grid.render();

        // then
        verify(substituteCriteria).orderDescBy("product.name");
    }

    @Test
    public void shouldRestrictResultsUsingExpression() throws Exception {
        // given
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());

        JSONObject jsonOrder = new JSONObject(ImmutableMap.of("column", "asd", "direction", "desc"));

        json.getJSONObject(ComponentState.JSON_CONTENT).put(GridComponentState.JSON_ORDER, jsonOrder);

        grid.initialize(json, Locale.ENGLISH);

        GridComponentColumn column = new GridComponentColumn("asd");
        column.setExpression("#product['name']");

        columns.put("asd", column);

        // when
        grid.render();

        // then
        // verify(substituteCriteria).restrictedWith(Restrictions.eq("product.name", "test"));
        verify(substituteCriteria).restrictedWith(Restrictions.forOperator(RestrictionOperator.EQ, null, "test"));
    }

    @Test
    public void shouldRestrictResultsUsingLike() throws Exception {
        // given
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());

        BelongsToType productFieldType = mock(BelongsToType.class);
        given(productFieldType.getDataDefinition()).willReturn(substituteDataDefinition);

        FieldDefinition productFieldDefinition = mock(FieldDefinition.class);
        given(productFieldDefinition.getType()).willReturn(productFieldType);

        FieldDefinition nameFieldDefinition = mock(FieldDefinition.class);
        given(nameFieldDefinition.getType()).willReturn(new StringType());

        given(substituteDataDefinition.getField("product")).willReturn(productFieldDefinition);
        given(substituteDataDefinition.getField("name")).willReturn(nameFieldDefinition);

        JSONObject jsonOrder = new JSONObject(ImmutableMap.of("column", "asd", "direction", "desc"));

        json.getJSONObject(ComponentState.JSON_CONTENT).put(GridComponentState.JSON_ORDER, jsonOrder);

        grid.initialize(json, Locale.ENGLISH);

        GridComponentColumn column = new GridComponentColumn("asd");
        column.setExpression("#product['name']");

        columns.put("asd", column);

        // when
        grid.render();

        // then
        verify(substituteCriteria).restrictedWith(Restrictions.forOperator(RestrictionOperator.EQ, null, "test*"));
    }

    @Test
    public void shouldIgnoreOrderWhenColumnHasMultipleFields() throws Exception {
        // given
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);

        FieldDefinition field1 = mock(FieldDefinition.class);
        given(field1.getName()).willReturn("asdName");

        FieldDefinition field2 = mock(FieldDefinition.class);
        given(field2.getName()).willReturn("asdName");

        GridComponentColumn column = new GridComponentColumn("asd");
        column.addField(field1);
        column.addField(field2);

        columns.put("asd", column);

        // when
        grid.render();

        // then
        verify(substituteCriteria, never()).orderAscBy(anyString());
        verify(substituteCriteria, never()).orderDescBy(anyString());
    }

    @Test
    public void shouldIgnoreRestrictionWhenColumnHasMultipleFields() throws Exception {
        // given
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);

        FieldDefinition field1 = mock(FieldDefinition.class);
        given(field1.getName()).willReturn("asdName");

        FieldDefinition field2 = mock(FieldDefinition.class);
        given(field2.getName()).willReturn("asdName");

        GridComponentColumn column = new GridComponentColumn("asd");
        column.addField(field1);
        column.addField(field2);

        columns.put("asd", column);

        // when
        grid.render();

        // then
        verify(substituteCriteria, times(1)).restrictedWith(any(Restriction.class));
    }

    @Test
    public void shouldSelectEntity() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent(viewDefinitionState, "select", new String[0]);

        // then
        verify(listener).onFieldEntityIdChange(13L);
    }

    @Test
    public void shouldRefresh() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent(viewDefinitionState, "refresh", new String[0]);
    }

    @Test
    public void shouldRemoveSelectedEntity() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        given(substituteDataDefinition.get(anyLong())).willReturn(entity);
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent(viewDefinitionState, "remove", new String[0]);

        // then
        verify(substituteDataDefinition).delete(Collections.singleton(13L));

        JSONObject json = grid.render();

        assertFalse(json.getJSONObject(ComponentState.JSON_CONTENT).has(GridComponentState.JSON_SELECTED_ENTITY_ID));
        assertEquals("SUCCESS",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("i18n",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener).onFieldEntityIdChange(null);
    }

    // @Test(expected = IllegalStateException.class)
    // public void shouldNotRemoveSelectedEntityOnFail() throws Exception {
    // // given
    // FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
    // SearchResult result = mock(SearchResult.class);
    // given(substituteCriteria.list()).willReturn(result);
    // given(result.getTotalNumberOfEntities()).willReturn(0);
    // given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
    // given(substituteDataDefinition.get(anyLong())).willReturn(entity);
    // willThrow(new IllegalStateException()).given(substituteDataDefinition).delete(13L);
    // grid.initialize(json, Locale.ENGLISH);
    // grid.addFieldEntityIdChangeListener("field", listener);
    //
    // // when
    // grid.performEvent(viewDefinitionState, "remove", new String[0]);
    // }

    @Test
    public void shouldCopySelectedEntity() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);
        Entity copiedEntity = new DefaultEntity(substituteDataDefinition, 14L, Collections.singletonMap("name",
                (Object) "text(1)"));
        given(substituteDataDefinition.copy(13L)).willReturn(copiedEntity);

        // when
        grid.performEvent(viewDefinitionState, "copy", new String[0]);

        // then
        verify(substituteDataDefinition).copy(Collections.singleton(13L));

        JSONObject json = grid.render();

        assertEquals(Long.valueOf(13L), grid.getSelectedEntityId());
        assertEquals("SUCCESS",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("i18n",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener, never()).onFieldEntityIdChange(13L);
    }

    @Test
    public void shouldModeUpSelectedEntity() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent(viewDefinitionState, "moveUp", new String[0]);

        // then
        verify(substituteDataDefinition).move(13L, -1);

        JSONObject json = grid.render();

        assertEquals("SUCCESS",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("i18n",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener, never()).onFieldEntityIdChange(13L);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotModeUpSelectedEntityOnFail() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        willThrow(new IllegalStateException()).given(substituteDataDefinition).move(13L, -1);
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent(viewDefinitionState, "moveUp", new String[0]);
    }

    @Test
    public void shouldModeDownSelectedEntity() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent(viewDefinitionState, "moveDown", new String[0]);

        // then
        verify(substituteDataDefinition).move(13L, 1);

        JSONObject json = grid.render();

        assertEquals("SUCCESS",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("i18n",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener, never()).onFieldEntityIdChange(13L);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotModeDownSelectedEntityOnFail() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        willThrow(new IllegalStateException()).given(substituteDataDefinition).move(13L, 1);
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent(viewDefinitionState, "moveDown", new String[0]);
    }

    @Test
    public void shouldGetValueUsingExpression() throws Exception {
        // given
        given(productDataDefinition.getField(anyString()).getType().toString(anyString(), any(Locale.class))).willReturn("John");

        GridComponentColumn column = new GridComponentColumn("name");
        column.setExpression("#name + ' ' + #id");

        Entity entity = new DefaultEntity(productDataDefinition, 13L, ImmutableMap.of("name", (Object) "John"));

        // when
        String value = column.getValue(entity, Locale.ENGLISH);

        // then
        assertEquals("John 13", value);
    }

    @Test
    public void shouldGetValueUsingField() throws Exception {
        // given
        FieldDefinition field = mock(FieldDefinition.class);
        given(field.getName()).willReturn("name");
        given(field.getValue("John", Locale.ENGLISH)).willReturn("Johny");

        GridComponentColumn column = new GridComponentColumn("name");
        column.addField(field);

        Entity entity = new DefaultEntity(productDataDefinition, 13L, ImmutableMap.of("name", (Object) "John"));

        // when
        String value = column.getValue(entity, Locale.ENGLISH);

        // then
        assertEquals("Johny", value);
    }

    @Test
    public void shouldGetValueUsingFields() throws Exception {
        // given
        FieldDefinition field1 = mock(FieldDefinition.class);
        given(field1.getName()).willReturn("name");
        given(field1.getValue("John", Locale.ENGLISH)).willReturn("Johny");

        FieldDefinition field2 = mock(FieldDefinition.class);
        given(field2.getName()).willReturn("lastname");
        given(field2.getValue("Smith", Locale.ENGLISH)).willReturn("Smithy");

        GridComponentColumn column = new GridComponentColumn("name");
        column.addField(field1);
        column.addField(field2);

        Entity entity = new DefaultEntity(productDataDefinition, 13L, ImmutableMap.of("name", (Object) "John", "lastname",
                (Object) "Smith"));

        // when
        String value = column.getValue(entity, Locale.ENGLISH);

        // then
        assertEquals("Johny, Smithy", value);
    }

}
