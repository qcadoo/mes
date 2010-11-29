package com.qcadoo.mes.newview;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

public class ViewDefinitionImpl implements ViewDefinition {

    private Map<String, ComponentPattern> componentPatterns = new HashMap<String, ComponentPattern>();

    public void initialize() {
        for (ComponentPattern componentPattern : componentPatterns.values()) {
            componentPattern.initialize(this);
        }
    }

    public Map<String, Object> prepareView(Locale locale) {
        // TODO mina

        return null;
    }

    public void performEvent(JSONObject object, Locale locale) {
        // TODO mina
    }

    public Map<String, ComponentPattern> getChildren() {
        return componentPatterns;
    }

    public ComponentPattern getChild(String name) {
        return componentPatterns.get(name);
    }

    public void addChild(ComponentPattern componentPattern) {
        componentPatterns.put(componentPattern.getName(), componentPattern);
    }

    public ComponentPattern getComponentByPath(String path) {
        String[] pathParts = path.split("\\.");
        ComponentPattern componentPattern = componentPatterns.get(pathParts[0]);
        if (componentPattern == null) {
            return null;
        }
        for (int i = 1; i < pathParts.length; i++) {
            ContainerPattern container = (ContainerPattern) componentPattern;
            componentPattern = container.getChild(pathParts[i]);
            if (componentPattern == null) {
                return null;
            }
        }
        return componentPattern;
    }
}
