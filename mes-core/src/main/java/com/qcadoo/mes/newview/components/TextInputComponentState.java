package com.qcadoo.mes.newview.components;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.newview.AbstractComponentState;

public class TextInputComponentState extends AbstractComponentState {

    private String value;

    @Override
    protected void initializeContent(final JSONObject json, final Locale locale) throws JSONException {
        value = json.getString("value");
    }

    @Override
    public void setFieldValue(final Object value) {
        this.value = (String) value;
        requestRender();
    }

    @Override
    public Object getFieldValue() {
        return value;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("value", value);
        return json;
    }

}
