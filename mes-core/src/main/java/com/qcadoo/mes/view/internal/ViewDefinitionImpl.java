package com.qcadoo.mes.view.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

public class ViewDefinitionImpl implements ViewDefinition {

    private final String name;

    private final String pluginIdentifier;

    private final DataDefinition dataDefinition;

    private final boolean menuAccessible;

    private final List<HookDefinition> postInitializeHooks = new ArrayList<HookDefinition>();

    private final List<HookDefinition> preInitializeHooks = new ArrayList<HookDefinition>();

    private final List<HookDefinition> preRenderHooks = new ArrayList<HookDefinition>();

    public ViewDefinitionImpl(final String name, final String pluginIdentifier, final DataDefinition dataDefinition,
            final boolean menuAccessible) {
        this.name = name;
        this.dataDefinition = dataDefinition;
        this.pluginIdentifier = pluginIdentifier;
        this.menuAccessible = menuAccessible;
    }

    private final Map<String, ComponentPattern> componentPatterns = new HashMap<String, ComponentPattern>();

    private ViewDefinitionStateFactory viewDefinitionStateFactory = new ViewDefinitionStateFactory() {

        @Override
        public ViewDefinitionState getInstance() {
            return new ViewDefinitionStateImpl();
        }
    };

    public void setViewDefinitionStateFactory(final ViewDefinitionStateFactory viewDefinitionStateFactory) {
        this.viewDefinitionStateFactory = viewDefinitionStateFactory;
    }

    public void initialize() {
        for (ComponentPattern componentPattern : componentPatterns.values()) {
            componentPattern.initialize(this);
        }
    }

    @Override
    public Map<String, Object> prepareView(final Locale locale) {
        // TODO mina

        return null;
    }

    @Override
    public JSONObject performEvent(final JSONObject object, final Locale locale) throws JSONException {
        ViewDefinitionState vds = viewDefinitionStateFactory.getInstance();
        for (ComponentPattern cp : componentPatterns.values()) {
            vds.addChild(cp.createComponentState());
        }
        vds.initialize(object, locale);
        for (ComponentPattern cp : componentPatterns.values()) {
            ((AbstractComponentPattern) cp).updateComponentStateListeners(vds);
        }

        JSONObject eventJson = object.getJSONObject("event");
        String eventName = eventJson.getString("name");
        String eventComponent = eventJson.has("component") ? eventJson.getString("component") : null;
        JSONArray eventArgsArray = eventJson.has("args") ? eventJson.getJSONArray("args") : new JSONArray();
        String[] eventArgs = new String[eventArgsArray.length()];
        for (int i = 0; i < eventArgsArray.length(); i++) {
            eventArgs[i] = eventArgsArray.getString(i);
        }
        vds.performEvent(eventComponent, eventName, eventArgs);

        vds.beforeRender();

        return vds.render();
    }

    @Override
    public Map<String, ComponentPattern> getChildren() {
        return componentPatterns;
    }

    @Override
    public ComponentPattern getChild(final String name) {
        return componentPatterns.get(name);
    }

    public void addChild(final ComponentPattern componentPattern) {
        componentPatterns.put(componentPattern.getName(), componentPattern);
    }

    @Override
    public ComponentPattern getComponentByPath(final String path) {
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    @Override
    public boolean isMenuAccessible() {
        return menuAccessible;
    }

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinition;
    };

    @Override
    public Set<String> getJavaScriptFilePaths() {
        Set<String> pathsSet = new HashSet<String>();
        updateJavaScriptFilePaths(pathsSet, componentPatterns.values());
        return pathsSet;
    }

    private void updateJavaScriptFilePaths(final Set<String> paths, final Iterable<ComponentPattern> componentPatterns) {
        for (ComponentPattern componentPattern : componentPatterns) {
            paths.add(componentPattern.getJavaScriptFilePath());
            if (componentPattern instanceof ContainerPattern) {
                updateJavaScriptFilePaths(paths, ((ContainerPattern) componentPattern).getChildren().values());
            }
        }
    }

    public void addPostInitializeHook(final HookDefinition hookDefinition) {
        postInitializeHooks.add(hookDefinition);
    }

    public void addPreRenderHook(final HookDefinition hookDefinition) {
        preRenderHooks.add(hookDefinition);
    }

    public void addPreInitializeHook(final HookDefinition hookDefinition) {
        preInitializeHooks.add(hookDefinition);
    }
}
