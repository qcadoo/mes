package com.qcadoo.mes.view.internal;

import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.states.AbstractContainerState;

public class ViewDefinitionStateImpl extends AbstractContainerState implements ViewDefinitionState {

    @Override
    public void initializeContent(final JSONObject json) throws JSONException {
        // empty method
    }

    @Override
    public JSONObject renderContent() throws JSONException {
        return null; // empty method
    }

    @Override
    public void performEvent(final String component, final String event, final String... args) {
        if (component != null) {
            getComponentByPath(component).performEvent(event, args);
        } else {
            performEventOnChildren(getChildren().values(), event, args);
        }
    }

    @Override
    public ComponentState getComponentByPath(final String path) {
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

    private void performEventOnChildren(final Collection<ComponentState> components, final String event, final String... args) {
        for (ComponentState component : components) {
            component.performEvent(event, args);
            if (component instanceof ContainerState) {
                performEventOnChildren(((ContainerState) component).getChildren().values(), event, args);
            }
        }
    }

}
