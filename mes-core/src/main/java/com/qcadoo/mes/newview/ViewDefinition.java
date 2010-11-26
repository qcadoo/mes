package com.qcadoo.mes.newview;

import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

public interface ViewDefinition {

    Map<String, Object> prepareView(Locale locale);

    void performEvent(JSONObject object, Locale locale);

}
