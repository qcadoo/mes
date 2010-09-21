package com.qcadoo.mes.core.data.view;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.DataDefinition;

public abstract class AbstractRootComponent extends AbstractContainerComponent<Object> implements RootComponent {

    private final Map<String, Component<?>> componentRegistry = new LinkedHashMap<String, Component<?>>();

    public AbstractRootComponent(final String name, final DataDefinition dataDefinition) {
        super(name, null, null, null);
        setDataDefinition(dataDefinition);
    }

    @Override
    public final boolean isInitialized() {
        return true;
    }

    @Override
    public final void initialize() {
        registerComponents(getComponents());
        initializeComponents(0);
    }

    @Override
    public CastableComponent<?> lookupComponent(final String path) {
        return componentRegistry.get(path);
    }

    @Override
    public Set<String> lookupListeners(final String path) {
        Set<String> paths = new HashSet<String>();
        getListenersForPath(path, paths);
        return paths;
    }

    @Override
    public final Object castContainerValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        return null;
    }

    @Override
    public final Object getContainerValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Object> viewValue, final Set<String> pathsToUpdate) {
        return null;
    }

    private void initializeComponents(final int previousNotInitialized) {
        int notInitialized = 0;

        for (InitializableComponent component : componentRegistry.values()) {
            if (component.isInitialized()) {
                continue;
            }
            if (!component.initializeComponent(componentRegistry)) {
                notInitialized++;
            }
        }

        if (notInitialized > 0) {
            if (previousNotInitialized == notInitialized) {
                for (InitializableComponent component : componentRegistry.values()) {
                    if (component.isInitialized()) {
                        continue;
                    }
                }
                throw new IllegalStateException("Cyclic dependency in view definition or misspelled referenced component name");
            } else {
                initializeComponents(notInitialized);
            }
        }
    }

    private void registerComponents(final Map<String, Component<?>> components) {
        for (Component<?> component : components.values()) {
            componentRegistry.put(component.getPath(), component);
            if (component instanceof ContainerComponent) {
                registerComponents(((ContainerComponent<?>) component).getComponents());
            }

        }
    }

    private void getListenersForPath(final String path, final Set<String> paths) {
        Set<String> listenerPaths = componentRegistry.get(path).getListeners();
        listenerPaths.removeAll(paths);
        paths.addAll(listenerPaths);
        for (String listenerPath : listenerPaths) {
            getListenersForPath(listenerPath, paths);
        }
    }

}
