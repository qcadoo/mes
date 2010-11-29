package com.qcadoo.mes.newview.components;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.newview.AbstractComponentState;
import com.qcadoo.mes.newview.FieldEntityIdChangeListener;

public class TestComponentState extends AbstractComponentState {

    @Override
    public void setFieldValue(Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getFieldValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void initializeContent(JSONObject json, Locale locale) throws JSONException {
        // TODO Auto-generated method stub

    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        // TODO Auto-generated method stub
        return null;
    }

    public final Map<String, FieldEntityIdChangeListener> getFieldEntityIdChangeListenersMap() {
        return getFieldEntityIdChangeListeners();
    }

}
