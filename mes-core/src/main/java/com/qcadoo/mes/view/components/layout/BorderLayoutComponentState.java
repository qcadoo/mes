package com.qcadoo.mes.view.components.layout;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.states.AbstractContainerState;

public class BorderLayoutComponentState extends AbstractContainerState {

    private String label;

    public BorderLayoutComponentState(final String label) {
        this.label = label;
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        if (json.has(JSON_VALUE) && !json.isNull(JSON_VALUE)) {
            label = json.getString(JSON_VALUE);
        }
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_VALUE, label);
        return json;
    }

    public void setLabel(final String label) {
        this.label = label;
        requestRender();
    }

    public String getLabel() {
        return label;
    }

}
