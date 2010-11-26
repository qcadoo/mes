package com.qcadoo.mes.newview.components;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.newview.AbstractComponentState;

public class TextInputComponentState extends AbstractComponentState {

    private String textValue;

    @Override
    protected void initializeContent(final JSONObject json, final Locale locale) throws JSONException {
        textValue = json.getString("value");
    }

    @Override
    public void setFieldValue(final Object value) {
        this.textValue = (String) value;
    }

    @Override
    public Object getFieldValue() {
        return textValue;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject jsonContent = new JSONObject();
        jsonContent.put("value", textValue);
        return jsonContent;
    }

}
