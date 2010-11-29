package com.qcadoo.mes.newview;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractContainerPattern extends AbstractComponentPattern implements ContainerPattern {

    private Map<String, ComponentPattern> children = new HashMap<String, ComponentPattern>();

    public AbstractContainerPattern(final String name, final String fieldPath, final String sourceFieldPath,
            final AbstractComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    public Map<String, ComponentPattern> getChildren() {
        return children;
    }

    public ComponentPattern getChild(String name) {
        return children.get(name);
    }

    public void addChild(ComponentPattern componentPattern) {
        children.put(componentPattern.getName(), componentPattern);
    }

    public void initialize(ViewDefinition viewDefinition) {
        super.initialize(viewDefinition);
        for (ComponentPattern componentPattern : children.values()) {
            componentPattern.initialize(viewDefinition);
        }
    }
}
