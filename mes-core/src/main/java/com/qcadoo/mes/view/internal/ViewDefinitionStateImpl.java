package com.qcadoo.mes.view.internal;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.states.AbstractContainerState;

public class ViewDefinitionStateImpl extends AbstractContainerState implements ViewDefinitionState {

    @Override
    public void setFieldValue(final Object value) {
        // empty method
    }

    @Override
    public Object getFieldValue() {
        // empty method
        return null;
    }

    @Override
    public void initializeContent(final JSONObject json) throws JSONException {
        // empty method
    }

    @Override
    public JSONObject renderContent() throws JSONException {
        // empty method
        return null;
    }

    @Override
    public void performEvent(final String path, final String event, final String... args) {
        getComponentByPath(path).performEvent(event, args);
    }

    @Override
    public ComponentState getComponentByPath(final String path) {
        String[] pathParts = path.split("\\.");
        System.out.println("----2--->");
        System.out.println(Arrays.toString(pathParts));
        System.out.println(getName());
        System.out.println(getChildren());
        ComponentState componentState = getChild(pathParts[0]);
        System.out.println(componentState);
        if (componentState == null) {
            return null;
        }
        for (int i = 1; i < pathParts.length; i++) {
            System.out.println("----");
            ContainerState container = (ContainerState) componentState;
            System.out.println(container);
            System.out.println(container.getName());
            System.out.println(container.getChildren());
            componentState = container.getChild(pathParts[i]);
            System.out.println(componentState);
            if (componentState == null) {
                return null;
            }
        }
        return componentState;
    }

}
