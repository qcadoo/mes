package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.model.DataDefinition;

public interface ViewDefinition {

    String JSON_EVENT = "event";

    String JSON_EVENT_NAME = "name";

    String JSON_EVENT_COMPONENT = "component";

    String JSON_EVENT_ARGS = "args";

    String JSON_COMPONENTS = "components";

    String JSON_JS_FILE_PATHS = "jsFilePaths";

    String getName();

    String getPluginIdentifier();

    Map<String, Object> prepareView(Locale locale);

    JSONObject performEvent(JSONObject object, Locale locale) throws JSONException;

    ComponentPattern getComponentByPath(String path);

    boolean isMenuAccessible();

    DataDefinition getDataDefinition();

    void addJsFilePath(String jsFilePath);

}
