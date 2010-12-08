package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.states.AbstractComponentState;

public class ButtonComponentState extends AbstractComponentState {

    private String value;

    public ButtonComponentState(final String url) {
        setFieldValue(url);
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        requestRender();
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_VALUE, value);
        return json;
    }

    @Override
    public final void setFieldValue(final Object value) {
        this.value = value != null ? value.toString() : null;
    }

    @Override
    public final Object getFieldValue() {
        return value;
    }

}
