package com.qcadoo.mes.plugins.products.controller;

import java.nio.channels.IllegalSelectorException;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;
import com.qcadoo.mes.core.data.definition.view.elements.grid.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.view.elements.grid.GridDefinition;

public class CrudControllerUtils {

    private CrudControllerUtils() {

    }

    public static void generateJsonViewElementOptions(final ComponentDefinition viewElement,
            final Map<String, String> viewElementsOptionsJson, String elementNamePath) {

        JSONObject obj = new JSONObject();
        try {
            String path = (elementNamePath != null ? elementNamePath + "-" : "") + viewElement.getName();
            obj.put("name", path);
            if (viewElement.getOptions() != null) {
                JSONObject optionsObject = new JSONObject();
                for (Entry<String, String> optionEntry : viewElement.getOptions().entrySet()) {
                    optionsObject.put(optionEntry.getKey(), optionEntry.getValue());
                }
                obj.put("options", optionsObject);
            }
            obj.put("listenable", viewElement.isLisinable());
            if (viewElement.getType() == ComponentDefinition.TYPE_ELEMENT_GRID) {
                if (!(viewElement instanceof GridDefinition)) {
                    throw new IllegalSelectorException();
                }
                GridDefinition gridDefinition = (GridDefinition) viewElement;
                if (gridDefinition.getCorrespondingViewName() != null) {
                    obj.put("correspondingViewName", gridDefinition.getCorrespondingViewName());
                }

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

            viewElementsOptionsJson.put(path, obj.toString());

            if (viewElement instanceof ContainerDefinition) {
                ContainerDefinition container = (ContainerDefinition) viewElement;
                for (ComponentDefinition component : container.getComponents().values()) {
                    generateJsonViewElementOptions(component, viewElementsOptionsJson, path);
                }
            }

        } catch (JSONException e) {

        }
    }
}
