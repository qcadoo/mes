package com.qcadoo.mes.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;

public abstract class AbstractRootComponent extends AbstractContainerComponent<Object> implements RootComponent {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRootComponent.class);

    private final Map<String, Component<?>> componentRegistry = new LinkedHashMap<String, Component<?>>();

    public AbstractRootComponent(final String name, final DataDefinition dataDefinition, final ViewDefinition viewDefinition,
            final TranslationService translationService) {
        super(name, null, null, null, translationService);
        setViewDefinition(viewDefinition);
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
        // for (Component<?> component : componentRegistry.values()) {
        // System.out.println(" 1 ----> " + component);
        // }
    }

    @Override
    public final Component<?> lookupComponent(final String path) {
        return componentRegistry.get(path);
    }

    @Override
    public final Set<String> lookupListeners(final String path) {
        Set<String> paths = new HashSet<String>();
        getListenersForPath(path, paths);
        return paths;
    }

    @Override
    public final void addContainerMessages(final Entity entity, final ViewValue<Object> viewValue) {
    }

    @Override
    public final Object castContainerValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        return null;
    }

    @Override
    public final Object getContainerValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Object> viewValue, final Set<String> pathsToUpdate) {
        return null;
    }

    private void initializeComponents(final int previousNotInitialized) {
        int notInitialized = 0;

        initializeComponent();

        for (Component<?> component : componentRegistry.values()) {
            if (component.isInitialized()) {
                continue;
            }
            if (!component.initializeComponent(componentRegistry)) {
                notInitialized++;
            }
        }

        if (notInitialized > 0) {
            if (previousNotInitialized == notInitialized) {
                for (Component<?> component : componentRegistry.values()) {
                    if (component.isInitialized()) {
                        continue;
                    }
                    LOG.info("Component not initialized: " + component);
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
        Component<?> component = componentRegistry.get(path);
        if (component == null) {
            return;
        }
        if (component instanceof ContainerComponent<?>) {
            Collection<Component<?>> children = ((ContainerComponent<?>) component).getComponents().values();
            Set<String> childrenPaths = new HashSet<String>();
            for (Component<?> child : children) {
                childrenPaths.add(child.getPath());
            }
            childrenPaths.removeAll(paths);
            paths.addAll(childrenPaths);
            for (String childerPath : childrenPaths) {
                getListenersForPath(childerPath, paths);
            }
        }
        Set<String> listenerPaths = new HashSet<String>(component.getListeners());
        listenerPaths.removeAll(paths);
        paths.addAll(listenerPaths);
        for (String listenerPath : listenerPaths) {
            getListenersForPath(listenerPath, paths);
        }
    }

}
