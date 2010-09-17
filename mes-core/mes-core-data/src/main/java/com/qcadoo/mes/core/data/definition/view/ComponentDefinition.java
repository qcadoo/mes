package com.qcadoo.mes.core.data.definition.view;

import java.util.HashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;

public abstract class ComponentDefinition {

    private final String name;

    private final String path;

    private final String fieldPath;

    private final String sourceFieldPath; // entity, entity.field[.field], #fieldName.field[.field]

    private final Map<String, String> options = new HashMap<String, String>();

    private final ContainerDefinition parentContainter;

    private final ComponentDefinition sourceComponent;

    private final DataDefinition dataDefinition;

    public abstract String getType();

    public abstract Object getValue(Entity entity, Map<String, Object> selectableValues, Object viewEntity);

    public ComponentDefinition(final String name, final ContainerDefinition parentContainer, final String fieldPath,
            final String sourceFieldPath, final DataDefinition dataDefinition) {
        this.name = name;
        this.parentContainter = parentContainer;
        this.fieldPath = fieldPath;

        if (sourceFieldPath.startsWith("#")) {
            this.sourceComponent = null; // TODO
            this.sourceFieldPath = sourceFieldPath;
            this.dataDefinition = sourceComponent.getDataDefinition();
        } else {
            this.sourceComponent = null;
            this.sourceFieldPath = sourceFieldPath;
            this.dataDefinition = dataDefinition;
        }

        if (parentContainer != null) {
            this.path = parentContainer.getPath() + "." + name;
        } else {
            this.path = name;
        }
    }

    public boolean isContainer() {
        return false;
    }

    public String getName() {
        return name;
    }

    public String getSourceFieldPath() {
        return sourceFieldPath;
    }

    public String getPath() {
        return path;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public Map<String, Object> getOptions() {
        Map<String, Object> viewOptions = new HashMap<String, Object>(options);
        viewOptions.put("name", name);
        return viewOptions;
    }

    protected ComponentDefinition getSourceComponent() {
        return sourceComponent;
    }

    protected ContainerDefinition getParentContainer() {
        return parentContainter;
    }

    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    public void addOptions(final String name, final String value) {
        this.options.put(name, value);
    }

}
