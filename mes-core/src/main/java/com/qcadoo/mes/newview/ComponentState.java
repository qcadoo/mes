package com.qcadoo.mes.newview;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public interface ComponentState {

    String getName();

    void initialize(JSONObject json, Locale locale) throws JSONException;

    void performEvent(String event, String... args);

    JSONObject render() throws JSONException;

    void setFieldValue(Object value);

    Object getFieldValue();

}
