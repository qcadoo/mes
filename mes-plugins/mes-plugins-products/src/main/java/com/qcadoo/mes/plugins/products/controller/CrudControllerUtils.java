package com.qcadoo.mes.plugins.products.controller;

import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.definition.ViewElementDefinition;

public class CrudControllerUtils {

    private CrudControllerUtils() {

    }

    public static String generateJsonViewElementOptions(ViewElementDefinition viewElement) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", viewElement.getName());
            obj.put("dataDefinition", viewElement.getDataDefinition().getEntityName());
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

            if (viewElement.getType() == ViewElementDefinition.TYPE_GRID) {
                GridDefinition gridDefinition = (GridDefinition) viewElement;
                JSONArray columnsArray = new JSONArray();
                for (ColumnDefinition column : gridDefinition.getColumns()) {
                    columnsArray.put(column.getName());
                }
                obj.put("columns", columnsArray);
            }

        } catch (JSONException e) {

        }
        return obj.toString();
    }
}
