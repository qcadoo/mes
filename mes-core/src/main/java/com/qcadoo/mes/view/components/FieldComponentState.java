package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.states.AbstractComponentState;

public class FieldComponentState extends AbstractComponentState {

    public static final String JSON_REQUIRED = "required";

    private String value;

    private boolean required;

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        if (json.has(JSON_VALUE) && !json.isNull(JSON_VALUE)) {
            value = json.getString(JSON_VALUE);
        }
        if (json.has(JSON_REQUIRED) && !json.isNull(JSON_REQUIRED)) {
            required = json.getBoolean(JSON_REQUIRED);
        }
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_VALUE, value);
        json.put(JSON_REQUIRED, required);
        return json;
    }

    @Override
    public void setFieldValue(final Object value) {
        this.value = value != null ? value.toString() : null;
        requestRender();
    }

    @Override
    public Object getFieldValue() {
        return value;
    }

    public final boolean isRequired() {
        return required;
    }

    public final void setRequired(final boolean required) {
        this.required = required;
    }

    public void requestComponentUpdateState() {
        requestUpdateState();
    }

}
