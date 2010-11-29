package com.qcadoo.mes.newview;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewDefinitionImpl implements ViewDefinition {

    private Map<String, ComponentPattern> componentPatterns = new HashMap<String, ComponentPattern>();

    private ViewDefinitionStateFactory viewDefinitionStateFactory = new ViewDefinitionStateFactory() {

        @Override
        public ViewDefinitionState getInstance() {
            return new ViewDefinitionStateImpl();
        }
    };

    public void setViewDefinitionStateFactory(ViewDefinitionStateFactory viewDefinitionStateFactory) {
        this.viewDefinitionStateFactory = viewDefinitionStateFactory;
    }

    public void initialize() {
        for (ComponentPattern componentPattern : componentPatterns.values()) {
            componentPattern.initialize(this);
        }
    }

    public Map<String, Object> prepareView(Locale locale) {
        // TODO mina

        return null;
    }

    public JSONObject performEvent(JSONObject object, Locale locale) throws JSONException {
        ViewDefinitionState vds = viewDefinitionStateFactory.getInstance();
        for (ComponentPattern cp : componentPatterns.values()) {
            vds.addChild(cp.createComponentState());
        }
        vds.initialize(object, locale);
        for (ComponentPattern cp : componentPatterns.values()) {
            ((AbstractComponentPattern) cp).updateComponentStateListeners(vds);
        }

        String eventName = object.getString("eventName");
        String eventComponent = object.getString("eventComponent");
        JSONArray eventArgsArray = object.getJSONArray("eventArgs");
        String[] eventArgs = new String[eventArgsArray.length()];
        for (int i = 0; i < eventArgsArray.length(); i++) {
            eventArgs[i] = eventArgsArray.getString(i);
        }
        vds.performEvent(eventComponent, eventName, eventArgs);

        vds.beforeRender();

        return vds.render();
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
