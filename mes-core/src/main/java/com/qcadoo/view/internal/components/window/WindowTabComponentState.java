package com.qcadoo.view.internal.components.window;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.view.internal.states.AbstractContainerState;

public class WindowTabComponentState extends AbstractContainerState {

    public WindowTabComponentState() {

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
