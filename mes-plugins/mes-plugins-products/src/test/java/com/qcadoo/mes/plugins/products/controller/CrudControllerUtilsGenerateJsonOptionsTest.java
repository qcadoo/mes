package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FormDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.definition.ViewElementDefinition;

public class CrudControllerUtilsGenerateJsonOptionsTest {

    @Test
    public void shouldTranslateFormElementDefinitionOptionsToJsonWhenAllFieldsAreNotNull() {
        // given
        ViewElementDefinition viewElement = new FormDefinition("testForm", new DataDefinition("testEntity"));

        Map<String, String> options = new HashMap<String, String>();
        options.put("option1", "ov1");
        options.put("option2", "ov2");
        viewElement.setOptions(options);

        Map<String, String> events = new HashMap<String, String>();
        events.put("event1", "ev1");
        events.put("event2", "ev2");
        viewElement.setEvents(events);

        viewElement.setParent("par");

        viewElement.setCorrespondingViewName("cv");
        viewElement.setCorrespondingViewModal(true);

        viewElement.setParentField("parField");

        // when
        String json = CrudControllerUtils.generateJsonViewElementOptions(viewElement);

        // then
        try {
            JSONObject obj = new JSONObject(json);
            assertEquals(8, obj.length());
            assertEquals("testForm", obj.get("name"));
            assertEquals("testEntity", obj.get("dataDefinition"));

            JSONObject optionsObj = obj.getJSONObject("options");
            assertEquals(2, optionsObj.length());
            assertEquals("ov1", optionsObj.get("option1"));
            assertEquals("ov2", optionsObj.get("option2"));

            JSONObject eventsObj = obj.getJSONObject("events");
            assertEquals(2, eventsObj.length());
            assertEquals("ev1", eventsObj.get("event1"));
            assertEquals("ev2", eventsObj.get("event2"));

            assertEquals("par", obj.get("parent"));

            assertEquals("cv", obj.get("correspondingViewName"));
            assertEquals(true, obj.get("isCorrespondingViewModal"));

            assertEquals("parField", obj.get("parentField"));
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    public void shouldTranslateFormElementDefinitionOptionsToJsonWhenSomeFieldsAreNull() {
        // given
        ViewElementDefinition viewElement = new FormDefinition("testForm", new DataDefinition("testEntity"));

        // when
        String json = CrudControllerUtils.generateJsonViewElementOptions(viewElement);

        // then
        try {
            JSONObject obj = new JSONObject(json);
            assertEquals(2, obj.length());
            assertEquals("testForm", obj.get("name"));
            assertEquals("testEntity", obj.get("dataDefinition"));
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void shouldTranslateGridElementDefinitionOptionsToJsonWhenSomeFieldsAreNull() {
        // given
        GridDefinition gridElement = new GridDefinition("testGrid", null);

        ColumnDefinition c1 = new ColumnDefinition("col1");
        ColumnDefinition c2 = new ColumnDefinition("col2");
        gridElement.setColumns(Arrays.asList(new ColumnDefinition[] { c1, c2 }));

        FieldDefinition f1 = new FieldDefinition("field1");
        FieldDefinition f2 = new FieldDefinition("field2");
        DataDefinition dataDefinition = new DataDefinition("testDD");
        dataDefinition.addField(f1);
        dataDefinition.addField(f2);
        gridElement.setDataDefinition(dataDefinition);

        // when
        String json = CrudControllerUtils.generateJsonViewElementOptions(gridElement);

        // then
        try {
            JSONObject obj = new JSONObject(json);
            assertEquals(4, obj.length());
            assertEquals("testGrid", obj.get("name"));
            assertEquals("testDD", obj.get("dataDefinition"));

            JSONArray colsArray = obj.getJSONArray("columns");
            assertEquals(2, colsArray.length());
            assertEquals("col1", colsArray.get(0));
            assertEquals("col2", colsArray.get(1));

            JSONArray fieldsArray = obj.getJSONArray("fields");
            assertEquals(2, fieldsArray.length());
            assertEquals("field1", fieldsArray.get(0));
            assertEquals("field2", fieldsArray.get(1));
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }
}
