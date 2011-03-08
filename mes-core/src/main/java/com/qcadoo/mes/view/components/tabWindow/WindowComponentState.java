package com.qcadoo.mes.view.components.tabWindow;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.states.AbstractContainerState;

public class WindowComponentState extends AbstractContainerState {

    public WindowComponentState(final WindowComponentPattern pattern) {

    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        requestRender();
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();

        List<String> errorTabs = new LinkedList<String>();
        for (Map.Entry<String, ComponentState> child : getChildren().entrySet()) {
            if (child.getValue().isHasError()) {
                errorTabs.add(child.getKey());
            }
        }
        JSONArray errors = new JSONArray();
        for (String tabName : errorTabs) {
            errors.put(tabName);
        }
        json.put("errors", errors);

        return json;
    }

}
