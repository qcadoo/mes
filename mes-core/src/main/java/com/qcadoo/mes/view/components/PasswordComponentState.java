package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

public class PasswordComponentState extends FieldComponentState {

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = super.renderContent();
        json.put(JSON_VALUE, "");
        return json;
    }

}
