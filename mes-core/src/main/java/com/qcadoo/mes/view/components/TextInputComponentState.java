package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.states.AbstractComponentState;

public class TextInputComponentState extends AbstractComponentState {

    private String value;

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        value = json.getString(JSON_VALUE);
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
        json.put(JSON_VALUE, value);
        return json;
    }

}
