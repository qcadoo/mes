package com.qcadoo.mes.plugins.products.controller;

import java.nio.channels.IllegalSelectorException;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.grid.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.grid.GridDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;

public class CrudControllerUtils {

    private CrudControllerUtils() {

    }

    public static String generateJsonViewElementOptions(final ComponentDefinition viewElement) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", viewElement.getName());
            obj.put("dataDefinition", viewElement.getDataDefinition().getName());
            obj.put("isDataDefinitionProritizable", viewElement.getDataDefinition().isPrioritizable());
            if (viewElement.getOptions() != null) {
                JSONObject optionsObject = new JSONObject();
                for (Entry<String, String> optionEntry : viewElement.getOptions().entrySet()) {
                    optionsObject.put(optionEntry.getKey(), optionEntry.getValue());
                }
                obj.put("options", optionsObject);
            }
            if (viewElement.getEvents() != null) {
                JSONObject eventsObject = new JSONObject();
                for (Entry<String, String> eventEntry : viewElement.getEvents().entrySet()) {
                    eventsObject.put(eventEntry.getKey(), eventEntry.getValue());
                }
                obj.put("events", eventsObject);
            }
            if (viewElement.getParent() != null) {
                obj.put("parent", viewElement.getParent());
            }
            if (viewElement.getCorrespondingViewName() != null) {
                obj.put("correspondingViewName", viewElement.getCorrespondingViewName());
                obj.put("isCorrespondingViewModal", viewElement.isCorrespondingViewModal());
            }
            if (viewElement.getParentField() != null) {
                obj.put("parentField", viewElement.getParentField());
            }

            if (viewElement.getType() == ComponentDefinition.TYPE_GRID) {
                if (!(viewElement instanceof GridDefinition)) {
                    throw new IllegalSelectorException();
                }
                GridDefinition gridDefinition = (GridDefinition) viewElement;
                JSONArray columnsArray = new JSONArray();
                for (ColumnDefinition column : gridDefinition.getColumns()) {
                    columnsArray.put(column.getName());
                }
                obj.put("columns", columnsArray);
                JSONArray fieldsArray = new JSONArray();
                for (Entry<String, DataFieldDefinition> field : gridDefinition.getDataDefinition().getFields().entrySet()) {
                    fieldsArray.put(field.getKey());
                }
                obj.put("fields", fieldsArray);
            }

        } catch (JSONException e) {

        }
        return obj.toString();
    }
}
