package com.qcadoo.mes.core.data.internal.view;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.view.CastableComponent;
import com.qcadoo.mes.core.data.view.ComponentDefinition;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.InitializableComponent;
import com.qcadoo.mes.core.data.view.RootComponent;

public abstract class RootContainerDefinitionImpl extends ContainerDefinitionImpl<Object> implements RootComponent {

    private final Map<String, ComponentDefinition<?>> componentRegistry = new LinkedHashMap<String, ComponentDefinition<?>>();

    public RootContainerDefinitionImpl(final String name, final DataDefinition dataDefinition) {
        super(name, null, null, null);
        setDataDefinition(dataDefinition);
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.qcadoo.mes.core.data.internal.view.RootComponent#initialize()
     */
    @Override
    public void initialize() {
        registerComponents(getComponents());
        initializeComponents(0);

        for (CastableComponent<?> component : componentRegistry.values()) {
            System.out.println(" ---> " + component);
        }

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
                for (InitializableComponent component : componentRegistry.values()) {
                    if (component.isInitialized()) {
                        continue;
                    }
                    System.out.println(" ---> " + component.toString());
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
            if (component instanceof ContainerDefinitionImpl) {
                registerComponents(((ContainerComponent<?>) component).getComponents());
            }

        }
    }

    /*
     * (non-Javadoc)
     * @see com.qcadoo.mes.core.data.internal.view.RootComponent#getListenersForPath(java.lang.String)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * @see com.qcadoo.mes.core.data.internal.view.RootComponent#getComponentForPath(java.lang.String)
     */
    @Override
    public CastableComponent<?> getComponentForPath(final String path) {
        return componentRegistry.get(path);
    }

}
