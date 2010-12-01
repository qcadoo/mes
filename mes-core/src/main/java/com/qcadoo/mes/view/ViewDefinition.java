package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.model.DataDefinition;

public interface ViewDefinition {

    String getName();

    String getPluginIdentifier();

    Map<String, Object> prepareView(Locale locale);

    JSONObject performEvent(JSONObject object, Locale locale) throws JSONException;

    ComponentPattern getComponentByPath(String path);

    Map<String, ComponentPattern> getChildren();

    ComponentPattern getChild(String name);

    boolean isMenuAccessible();

    DataDefinition getDataDefinition();

    Set<String> getJavaScriptFilePaths();

}
