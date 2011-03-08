package com.qcadoo.mes.view.components.tabWindow;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.states.AbstractContainerState;

public class WindowTabComponentState extends AbstractContainerState {

    public WindowTabComponentState(final WindowTabComponentPattern pattern) {

    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();
        return json;
    }

}
