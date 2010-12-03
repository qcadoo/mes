package com.qcadoo.mes.view.states.components;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.components.GridComponentPattern.Column;
import com.qcadoo.mes.view.components.GridComponentState;
import com.qcadoo.mes.view.states.AbstractStateTest;

public class GridComponentStateTest extends AbstractStateTest {

    private Entity entity;

    private GridComponentState grid;

    private DataDefinition productDataDefinition;

    private DataDefinition substituteDataDefinition;

    private FieldDefinition substitutesFieldDefinition;

    private JSONObject json;

    private List<Column> columns;

    private SearchCriteriaBuilder substituteCriteria;

    @Before
    public void init() throws Exception {
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(GridComponentState.JSON_SELECTED_ENTITY_ID, 13L);
        jsonContent.put(GridComponentState.JSON_SCOPE_ENTITY_ID, 1L);
        jsonContent.put(GridComponentState.JSON_FIRST_ENTITY, 10);
        jsonContent.put(GridComponentState.JSON_MAX_ENTITIES, 30);
        jsonContent.put(GridComponentState.JSON_FILTERS_ENABLED, true);

        JSONObject jsonOrder = new JSONObject(Collections.singletonMap("asd", true));

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

        columns = new ArrayList<Column>();

        grid = new GridComponentState(substitutesFieldDefinition, columns);
        grid.setDataDefinition(substituteDataDefinition);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldInitialize() throws Exception {
        // when
        grid.initialize(json, Locale.ENGLISH);

        // then
        assertEquals(substitutesFieldDefinition, getField(grid, "scopeField"));
        assertEquals(13L, getField(grid, "selectedEntityId"));
        assertEquals(1L, getField(grid, "scopeEntityId"));
        assertNull(getField(grid, "entities"));
        assertEquals(0, getField(grid, "totalEntities"));
        assertEquals(10, getField(grid, "firstResult"));
        assertEquals(30, getField(grid, "maxResults"));
        assertTrue((Boolean) getField(grid, "filtersEnabled"));

        Map<String, Boolean> order = (Map<String, Boolean>) getField(grid, "order");

        assertEquals(1, order.size());
        assertTrue(order.get("asd"));

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
        assertNull(getField(grid, "scopeField"));
        assertNull(getField(grid, "selectedEntityId"));
        assertNull(getField(grid, "scopeEntityId"));
        assertNull(getField(grid, "entities"));
        assertEquals(0, getField(grid, "totalEntities"));
        assertEquals(0, getField(grid, "firstResult"));
        assertEquals(Integer.MAX_VALUE, getField(grid, "maxResults"));
        assertFalse((Boolean) getField(grid, "filtersEnabled"));

        Map<String, Boolean> order = (Map<String, Boolean>) getField(grid, "order");

        assertEquals(0, order.size());

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
        assertEquals(1L, json.getLong(GridComponentState.JSON_SCOPE_ENTITY_ID));
        assertEquals(10, json.getInt(GridComponentState.JSON_FIRST_ENTITY));
        assertEquals(30, json.getInt(GridComponentState.JSON_MAX_ENTITIES));
        assertTrue(json.getBoolean(GridComponentState.JSON_FILTERS_ENABLED));
        assertTrue(json.getJSONObject(GridComponentState.JSON_ORDER).getBoolean("asd"));
        assertEquals("test", json.getJSONObject(GridComponentState.JSON_FILTERS).getString("asd"));
        assertEquals("test2", json.getJSONObject(GridComponentState.JSON_FILTERS).getString("qwe"));
        assertEquals(0, json.getInt(GridComponentState.JSON_TOTAL_ENTITIES));
        assertEquals(0, json.getJSONArray(GridComponentState.JSON_ENTITIES).length());
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
