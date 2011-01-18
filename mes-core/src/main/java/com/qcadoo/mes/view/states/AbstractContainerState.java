package com.qcadoo.mes.view.states;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;

public abstract class AbstractContainerState extends AbstractComponentState implements ContainerState {

    private final Map<String, ComponentState> children = new HashMap<String, ComponentState>();

    @Override
    public final void initialize(final JSONObject json, final Locale locale) throws JSONException {
        super.initialize(json, locale);

        JSONObject childerJson = null;
        if (json.has(JSON_CHILDREN)) {
            childerJson = json.getJSONObject(JSON_CHILDREN);
        }

        for (Map.Entry<String, ComponentState> child : children.entrySet()) {
            if (childerJson == null) {
                child.getValue().initialize(new JSONObject(), locale);
            } else {
                child.getValue().initialize(childerJson.getJSONObject(child.getKey()), locale);
            }
        }
    }

    @Override
    public JSONObject render() throws JSONException {
        JSONObject json = super.render();

        JSONObject childerJson = new JSONObject();

        for (Map.Entry<String, ComponentState> child : children.entrySet()) {
            childerJson.put(child.getKey(), child.getValue().render());
        }

        json.put(JSON_CHILDREN, childerJson);

        return json;
    }

    @Override
    public final Map<String, ComponentState> getChildren() {
        return children;
    }

    @Override
    public final ComponentState getChild(final String name) {
        return children.get(name);
    }

    @Override
    public final void addChild(final ComponentState state) {
        children.put(state.getName(), state);
    }

}
