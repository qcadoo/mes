package com.qcadoo.mes.view.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.states.AbstractContainerState;

public final class ViewDefinitionStateImpl extends AbstractContainerState implements ViewDefinitionState {

    private String redirectToUrl;

    private boolean openInNewWindow;

    private final Map<String, ComponentState> components = new HashMap<String, ComponentState>();

    public ViewDefinitionStateImpl() {
        requestRender();
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty
    }

    @Override
    public final JSONObject render() throws JSONException {
        if (redirectToUrl != null) {
            JSONObject json = new JSONObject();
            JSONObject jsonRedirect = new JSONObject();
            json.put("redirect", jsonRedirect);
            jsonRedirect.put("url", redirectToUrl);
            jsonRedirect.put("openInNewWindow", openInNewWindow);
            return json;
        } else {
            return super.render();
        }
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();

        boolean isOk = true;

        List<ComponentState> states = getStatesAsList(getChildren().values());

        for (ComponentState state : states) {
            if (state.isHasError()) {
                isOk = false;
                break;
            }
        }

        json.put("status", isOk ? "ok" : "error");

        return json;
    }

    @Override
    public void performEvent(final String component, final String event, final String... args) {
        if (component != null) {
            getComponentByPath(component).performEvent(this, event, args);
        } else {
            performEventOnChildren(getChildren().values(), event, args);
        }
    }

    private ComponentState getComponentByPath(final String path) {
        ComponentState componentState = this;
        String[] pathParts = path.split("\\.");
        for (int i = 0; i < pathParts.length; i++) {
            ContainerState container = (ContainerState) componentState;
            componentState = container.getChild(pathParts[i]);

            if (componentState == null) {
                return null;
            }
        }
        return componentState;
    }

    @Override
    public ComponentState getComponentByReference(final String reference) {
        return components.get(reference);
    }

    private void performEventOnChildren(final Collection<ComponentState> components, final String event, final String... args) {
        for (ComponentState component : components) {
            component.performEvent(this, event, args);
            if (component instanceof ContainerState) {
                performEventOnChildren(((ContainerState) component).getChildren().values(), event, args);
            }
        }
    }

    private List<ComponentState> getStatesAsList(final Collection<ComponentState> states) {
        List<ComponentState> list = new ArrayList<ComponentState>();
        list.addAll(states);
        for (ComponentState state : states) {
            if (state instanceof ContainerState) {
                list.addAll(getStatesAsList(((ContainerState) state).getChildren().values()));
            }
        }
        return list;
    }

    @Override
    public void redirectTo(final String redirectToUrl, final boolean openInNewWindow) {
        this.redirectToUrl = redirectToUrl;
        this.openInNewWindow = openInNewWindow;
    }

    @Override
    public void registerComponent(final String reference, final String path, final ComponentState state) {
        components.put(reference, state);
    }

}
