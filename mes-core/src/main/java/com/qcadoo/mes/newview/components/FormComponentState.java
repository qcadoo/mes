package com.qcadoo.mes.newview.components;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.newview.AbstractContainerState;

public class FormComponentState extends AbstractContainerState {

    private Long value;

    public FormComponentState() {
        FormEventPerformer eventPerformer = new FormEventPerformer();
        registerEvent("clear", eventPerformer, "clear");
    }

    @Override
    protected void initializeContent(final JSONObject json, final Locale locale) throws JSONException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setFieldValue(final Object value) {
        this.value = (Long) value;
        notifyEntityIdChangeListeners((Long) value);
    }

    @Override
    public Object getFieldValue() {
        return value;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        // TODO Auto-generated method stub
        return null;
    }

    protected class FormEventPerformer {

        public void save() {

        }

        public void clear(final String[] args) {
            setFieldValue(null);
        }

    }
}
