package com.qcadoo.mes.view.states;

import org.json.JSONException;
import org.json.JSONObject;

public class ContainerStateMock extends AbstractContainerState {

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty

    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        // empty
        return null;
    }

}
