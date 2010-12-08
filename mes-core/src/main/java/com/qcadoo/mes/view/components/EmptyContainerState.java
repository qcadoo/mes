package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.states.AbstractContainerState;

public final class EmptyContainerState extends AbstractContainerState {

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        return new JSONObject();
    }

}
