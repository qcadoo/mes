package com.qcadoo.mes.core.data.definition.view;

import java.util.HashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.definition.DataDefinition;

public class WindowDefinition extends ContainerDefinition {

    private final DataDefinition rootDataDefinition;

    private final Map<String, ComponentDefinition> components = new HashMap<String, ComponentDefinition>();

    public WindowDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, null, null, null);
        rootDataDefinition = dataDefinition;
    }

    @Override
    public DataDefinition getDataDefinition() {
        return rootDataDefinition;
    }

    @Override
    public ComponentDefinition lookupComponent(final String path) {
        return components.get(path);
    }

    @Override
    public void registerComponent(final ComponentDefinition componentDefinition) {
        components.put(componentDefinition.getPath(), componentDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

}
