package com.qcadoo.mes.newview;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewDefinitionStateImpl extends AbstractContainerState implements ViewDefinitionState {

    @Override
    public void setFieldValue(Object value) {
        // empty method
    }

    @Override
    public Object getFieldValue() {
        // empty method
        return null;
    }

    @Override
    protected void initializeContent(JSONObject json) throws JSONException {
        // empty method
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        // empty method
        return null;
    }

    public void performEvent(String path, String event, String... args) {
        getComponentByPath(path).performEvent(event, args);
    }

    public ComponentState getComponentByPath(String path) {
        String[] pathParts = path.split("\\.");
        ComponentState componentState = getChild(pathParts[0]);
        if (componentState == null) {
            return null;
        }
        for (int i = 1; i < pathParts.length; i++) {
            ContainerState container = (ContainerState) componentState;
            componentState = container.getChild(pathParts[i]);
            if (componentState == null) {
                return null;
            }
        }
        return componentState;
    }

}
