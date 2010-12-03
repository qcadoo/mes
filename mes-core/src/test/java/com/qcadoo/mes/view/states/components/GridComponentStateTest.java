package com.qcadoo.mes.view.states.components;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restriction;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.components.GridComponentColumn;
import com.qcadoo.mes.view.components.GridComponentState;
import com.qcadoo.mes.view.states.AbstractStateTest;

public class GridComponentStateTest extends AbstractStateTest {

    private Entity entity;

    private GridComponentState grid;

    private DataDefinition productDataDefinition;

    private DataDefinition substituteDataDefinition;

    private FieldDefinition substitutesFieldDefinition;

    private JSONObject json;

    private Map<String, GridComponentColumn> columns;

    private SearchCriteriaBuilder substituteCriteria;

    @Before
    public void init() throws Exception {
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(GridComponentState.JSON_SELECTED_ENTITY_ID, 13L);
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

        productDataDefinition = mock(DataDefinition.class);
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

        grid = new GridComponentState(substitutesFieldDefinition, columns);
        grid.setDataDefinition(substituteDataDefinition);
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
        grid = new GridComponentState(null, columns);
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
        assertFalse((Boolean) getField(grid, "filtersEnabled"));

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
        verify(substituteCriteria).restrictedWith(Restrictions.eq("asdName", "test"));
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
        verify(substituteCriteria).restrictedWith(Restrictions.eq("product.name", "test"));
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
        grid.performEvent("select", new String[0]);

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
        grid.performEvent("refresh", new String[0]);
    }

    @Test
    public void shouldRemoveSelectedEntity() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent("remove", new String[0]);

        // then
        verify(substituteDataDefinition).delete(13L);

        JSONObject json = grid.render();

        assertFalse(json.getJSONObject(ComponentState.JSON_CONTENT).has(GridComponentState.JSON_SELECTED_ENTITY_ID));
        assertEquals(1, json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getInt(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("TODO - usunięto",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener).onFieldEntityIdChange(null);
    }

    @Test
    public void shouldNotRemoveSelectedEntityOnFail() throws Exception {
        // given
        FieldEntityIdChangeListener listener = mock(FieldEntityIdChangeListener.class);
        SearchResult result = mock(SearchResult.class);
        given(substituteCriteria.list()).willReturn(result);
        given(result.getTotalNumberOfEntities()).willReturn(0);
        given(result.getEntities()).willReturn(Collections.<Entity> emptyList());
        willThrow(new IllegalStateException()).given(substituteDataDefinition).delete(13L);
        grid.initialize(json, Locale.ENGLISH);
        grid.addFieldEntityIdChangeListener("field", listener);

        // when
        grid.performEvent("remove", new String[0]);

        // then
        JSONObject json = grid.render();

        assertEquals(13L, json.getJSONObject(ComponentState.JSON_CONTENT).getLong(GridComponentState.JSON_SELECTED_ENTITY_ID));
        assertEquals(0, json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getInt(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("TODO - nieusunięto - null",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener, never()).onFieldEntityIdChange(null);
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
        grid.performEvent("moveUp", new String[0]);

        // then
        verify(substituteDataDefinition).move(13L, -1);

        JSONObject json = grid.render();

        assertEquals(1, json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getInt(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("TODO - przesunięto",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener, never()).onFieldEntityIdChange(13L);
    }

    @Test
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
        grid.performEvent("moveUp", new String[0]);

        // then
        verify(substituteDataDefinition).move(13L, -1);

        JSONObject json = grid.render();

        assertEquals(0, json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getInt(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("TODO - nieprzesunięto - null",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener, never()).onFieldEntityIdChange(13L);
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
        grid.performEvent("moveDown", new String[0]);

        // then
        verify(substituteDataDefinition).move(13L, 1);

        JSONObject json = grid.render();

        assertEquals(1, json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getInt(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("TODO - przesunięto",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener, never()).onFieldEntityIdChange(13L);
    }

    @Test
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
        grid.performEvent("moveDown", new String[0]);

        // then
        verify(substituteDataDefinition).move(13L, 1);

        JSONObject json = grid.render();

        assertEquals(0, json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getInt(ComponentState.JSON_MESSAGE_TYPE));
        assertEquals("TODO - nieprzesunięto - null",
                json.getJSONArray(ComponentState.JSON_MESSAGES).getJSONObject(0).getString(ComponentState.JSON_MESSAGE_BODY));
        verify(listener, never()).onFieldEntityIdChange(13L);
    }

    // @Test
    // public void shouldRenderFormEntityId() throws Exception {
    // // given
    // ComponentState componentState = new FormComponentState(null);
    // componentState.setFieldValue(13L);
    //
    // // when
    // JSONObject json = componentState.render();
    //
    // // then
    // assertEquals(13L, json.getJSONObject(ComponentState.JSON_CONTENT).getLong(FormComponentState.JSON_ENTITY_ID));
    // }
    //
    // @Test
    // public void shouldClearFormIfEntityIsNotExists() throws Exception {
    // // given
    // form.setFieldValue(12L);
    //
    // // when
    // form.performEvent("initialize", new String[0]);
    //
    // // then
    // assertNull(form.getFieldValue());
    // }
    //
    // @Test
    // public void shouldResetForm() throws Exception {
    // // given
    // form.setFieldValue(13L);
    //
    // // when
    // form.performEvent("reset", new String[0]);
    //
    // // then
    // verify(name).setFieldValue("text");
    // }
    //
    // @Test
    // public void shouldClearFormEntity() throws Exception {
    // // given
    // form.setFieldValue(13L);
    //
    // // when
    // form.performEvent("clear", new String[0]);
    //
    // // then
    // verify(name).setFieldValue(null);
    // assertNull(form.getFieldValue());
    // }
    //
    // @Test
    // public void shouldDeleteFormEntity() throws Exception {
    // // given
    // form.setFieldValue(13L);
    //
    // // when
    // form.performEvent("delete", new String[0]);
    //
    // // then
    // verify(name).setFieldValue(null);
    // verify(categoryDataDefinition).delete(13L);
    // assertNull(form.getFieldValue());
    // }
    //
    // @Test
    // public void shouldSaveFormEntity() throws Exception {
    // // given
    // Entity entity = new DefaultEntity("plugin", "name", null, Collections.singletonMap("name", (Object) "text"));
    // Entity savedEntity = new DefaultEntity("plugin", "name", 13L, Collections.singletonMap("name", (Object) "text2"));
    // given(categoryDataDefinition.save(eq(entity))).willReturn(savedEntity);
    // given(name.getFieldValue()).willReturn("text");
    //
    // form.setFieldValue(null);
    //
    // // when
    // form.performEvent("save", new String[0]);
    //
    // // then
    // verify(categoryDataDefinition).save(eq(entity));
    // verify(name).setFieldValue("text2");
    // assertEquals(13L, form.getFieldValue());
    // assertTrue(((FormComponentState) form).isValid());
    // }
    //
    // @Test
    // public void shouldUseContextWhileSaving() throws Exception {
    // // given
    // Entity entity = new DefaultEntity("plugin", "name", 14L, Collections.singletonMap("name", (Object) "text2"));
    // Entity savedEntity = new DefaultEntity("plugin", "name", 14L, Collections.singletonMap("name", (Object) "text2"));
    // given(categoryDataDefinition.save(eq(entity))).willReturn(savedEntity);
    // given(name.getFieldValue()).willReturn("text");
    //
    // JSONObject json = new JSONObject();
    // JSONObject jsonContext = new JSONObject();
    // jsonContext.put("id", 14L);
    // jsonContext.put("name", "text2");
    // JSONObject jsonContent = new JSONObject();
    // jsonContent.put(FormComponentState.JSON_ENTITY_ID, 13L);
    // json.put(ComponentState.JSON_CONTEXT, jsonContext);
    // json.put(ComponentState.JSON_CONTENT, jsonContent);
    // json.put(ComponentState.JSON_CHILDREN, new JSONObject());
    //
    // form.initialize(json, Locale.ENGLISH);
    //
    // // when
    // form.performEvent("save", new String[0]);
    //
    // // then
    // verify(categoryDataDefinition).save(eq(entity));
    // verify(name).setFieldValue("text2");
    // assertEquals(14L, form.getFieldValue());
    // assertTrue(((FormComponentState) form).isValid());
    // }
    //
    // @Test
    // public void shouldHaveValidationErrors() throws Exception {
    // // given
    // Entity entity = new DefaultEntity("plugin", "name", null, Collections.singletonMap("name", (Object) "text"));
    // Entity savedEntity = new DefaultEntity("plugin", "name", null, Collections.singletonMap("name", (Object) "text2"));
    // savedEntity.addGlobalError("global.error");
    // savedEntity.addError(productsFieldDefinition, "field.error");
    //
    // given(translationService.translate(eq("global.error"), any(Locale.class))).willReturn("translated global error");
    // given(translationService.translate(eq("field.error"), any(Locale.class))).willReturn("translated field error");
    // given(categoryDataDefinition.save(eq(entity))).willReturn(savedEntity);
    // given(name.getFieldValue()).willReturn("text");
    //
    // form.setFieldValue(null);
    //
    // // when
    // form.performEvent("save", new String[0]);
    //
    // // then
    // verify(categoryDataDefinition).save(eq(entity));
    // assertFalse(((FormComponentState) form).isValid());
    // verify(name).addMessage("translated field error", ComponentState.MessageType.FAILURE);
    // assertTrue(form.render().toString().contains("translated global error"));
    // }
}
