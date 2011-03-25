/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.view.internal.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.view.internal.ComponentState;
import com.qcadoo.view.internal.ContainerState;
import com.qcadoo.view.internal.ViewDefinitionState;
import com.qcadoo.view.internal.states.AbstractContainerState;

public final class ViewDefinitionStateImpl extends AbstractContainerState implements ViewDefinitionState {

    private String redirectToUrl;

    private boolean openInNewWindow;

    private boolean openInModalWindow;

    private boolean shouldSerializeWindow;

    private final Map<String, ComponentState> registry = new HashMap<String, ComponentState>();

    public ViewDefinitionStateImpl() {
        requestRender();
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty
    }

    @Override
    public JSONObject render() throws JSONException {
        if (redirectToUrl != null) {
            JSONObject json = new JSONObject();
            JSONObject jsonRedirect = new JSONObject();
            json.put("redirect", jsonRedirect);
            jsonRedirect.put("url", redirectToUrl);
            jsonRedirect.put("openInNewWindow", openInNewWindow);
            jsonRedirect.put("openInModalWindow", openInModalWindow);
            jsonRedirect.put("shouldSerializeWindow", shouldSerializeWindow);
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
        return registry.get(reference);
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
    public void redirectTo(final String redirectToUrl, final boolean openInNewWindow, final boolean shouldSerialize) {
        this.redirectToUrl = redirectToUrl;
        this.openInNewWindow = openInNewWindow;
        openInModalWindow = false;
        this.shouldSerializeWindow = shouldSerialize;
    }

    @Override
    public void openModal(final String url) {
        this.redirectToUrl = url;
        this.openInNewWindow = false;
        openInModalWindow = true;
        this.shouldSerializeWindow = true;
    }

    @Override
    public void registerComponent(final String reference, final String path, final ComponentState state) {
        if (registry.containsKey(reference)) {
            throw new IllegalStateException("Duplicated state reference : " + reference);
        }
        registry.put(reference, state);
    }

}
