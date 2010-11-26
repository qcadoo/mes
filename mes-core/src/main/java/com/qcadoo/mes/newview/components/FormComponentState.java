package com.qcadoo.mes.newview.components;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.newview.AbstractContainerState;

public class FormComponentState extends AbstractContainerState {

    @Override
    protected void initializeContent(final JSONObject json, final Locale locale) throws JSONException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setFieldValue(final Object value) {
        notifyEntityIdChangeListeners((Long) value);
    }

    @Override
    public Object getFieldValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        // TODO Auto-generated method stub
        return null;
    }

}
