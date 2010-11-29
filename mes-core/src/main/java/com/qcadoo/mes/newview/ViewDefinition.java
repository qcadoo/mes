package com.qcadoo.mes.newview;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public interface ViewDefinition {

    Map<String, Object> prepareView(Locale locale);

    JSONObject performEvent(JSONObject object, Locale locale) throws JSONException;

    ComponentPattern getComponentByPath(String path);

}
