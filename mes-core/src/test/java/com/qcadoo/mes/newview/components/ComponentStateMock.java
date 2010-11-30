package com.qcadoo.mes.newview.components;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.newview.AbstractComponentState;
import com.qcadoo.mes.newview.FieldEntityIdChangeListener;

public class ComponentStateMock extends AbstractComponentState {

    private int beforeRenderContentCallNumber = 0;

    public ComponentStateMock(String name) {
        setName(name);
    }

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
    protected void initializeContent(JSONObject json) throws JSONException {
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

    @Override
    protected void beforeRenderContent() {
        beforeRenderContentCallNumber++;
    }

    public int getBeforeRenderContentCallNumber() {
        return beforeRenderContentCallNumber;
    }

}
