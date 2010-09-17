package com.qcadoo.mes.core.data.definition.view;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.core.data.definition.DataDefinition;

public abstract class RootContainerDefinition extends ContainerDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentDefinition.class);

    private final Map<String, ComponentDefinition> componentRegistry = new LinkedHashMap<String, ComponentDefinition>();

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
    }

    private void initializeComponents(final int previousNotInitialized) {
        int notInitialized = 0;

        for (ComponentDefinition component : componentRegistry.values()) {
            if (component.isInitialized()) {
                continue;
            }
            if (!component.initializeComponent(componentRegistry)) {
                notInitialized++;
            }
        }

        if (notInitialized > 0) {
            if (previousNotInitialized == notInitialized) {
                for (ComponentDefinition component : componentRegistry.values()) {
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

    private void registerComponents(final Map<String, ComponentDefinition> components) {
        for (ComponentDefinition component : components.values()) {
            componentRegistry.put(component.getPath(), component);
            if (component instanceof ContainerDefinition) {
                registerComponents(((ContainerDefinition) component).getComponents());
            }

        }
    }

}
