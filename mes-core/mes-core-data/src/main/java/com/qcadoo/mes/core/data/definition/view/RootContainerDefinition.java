package com.qcadoo.mes.core.data.definition.view;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;

public abstract class RootContainerDefinition extends ContainerDefinition<Object> {

    private final Map<String, ComponentDefinition<?>> componentRegistry = new LinkedHashMap<String, ComponentDefinition<?>>();

    public RootContainerDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, null, null, null);
        setDataDefinition(dataDefinition);
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    public void initialize() {
        registerComponents(getComponents());
        initializeComponents(0);

        for (ComponentDefinition<?> component : componentRegistry.values()) {
            System.out.println(" ---> " + component);
        }

    }

    private void initializeComponents(final int previousNotInitialized) {
        int notInitialized = 0;

        for (ComponentDefinition<?> component : componentRegistry.values()) {
            if (component.isInitialized()) {
                continue;
            }
            if (!component.initializeComponent(componentRegistry)) {
                notInitialized++;
            }
        }

        if (notInitialized > 0) {
            if (previousNotInitialized == notInitialized) {
                for (ComponentDefinition<?> component : componentRegistry.values()) {
                    if (component.isInitialized()) {
                        continue;
                    }
                }
                throw new IllegalStateException("Cyclic dependency in view definition");
            } else {
                initializeComponents(notInitialized);
            }
        }
    }

    private void registerComponents(final Map<String, ComponentDefinition<?>> components) {
        for (ComponentDefinition<?> component : components.values()) {
            componentRegistry.put(component.getPath(), component);
            if (component instanceof ContainerDefinition) {
                registerComponents(((ContainerDefinition<?>) component).getComponents());
            }

        }
    }

    public Set<String> getListenersForPath(final String path) {
        Set<String> paths = new HashSet<String>();
        getListenersForPath(path, paths);
        return paths;
    }

    private void getListenersForPath(final String path, final Set<String> paths) {
        Set<String> listenerPaths = componentRegistry.get(path).getListeners();
        listenerPaths.removeAll(paths);
        paths.addAll(listenerPaths);
        for (String listenerPath : listenerPaths) {
            getListenersForPath(listenerPath, paths);
        }
    }

    @Override
    public Object castContainerValue(final Entity entity, final Object viewObject) {
        return null;
    }

    @Override
    public Object getContainerValue(final Entity entity, final Map<String, List<Entity>> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final ViewEntity<Object> viewEntity) {
        return null;
    }

}
