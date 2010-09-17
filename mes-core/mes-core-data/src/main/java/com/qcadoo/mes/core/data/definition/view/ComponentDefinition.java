package com.qcadoo.mes.core.data.definition.view;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;

public abstract class ComponentDefinition {

    private final String name;

    private final String path;

    private final String fieldPath;

    private final String sourceFieldPath;

    private final Map<String, String> options = new HashMap<String, String>();

    private final ContainerDefinition parentContainter;

    private final ComponentDefinition sourceComponent;

    private final Set<String> listeners = new HashSet<String>();

    private final DataDefinition dataDefinition;

    public abstract String getType();

    public abstract Object getValue(Entity entity, Map<String, Object> selectableValues, Object viewEntity);

    public ComponentDefinition(final String name, final ContainerDefinition parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        this.name = name;
        this.parentContainter = parentContainer;
        this.fieldPath = fieldPath;

        if (parentContainer != null) {
            this.path = parentContainer.getPath() + "." + name;
        } else {
            this.path = name;
        }

        if (sourceFieldPath != null && sourceFieldPath.startsWith("#")) {
            String[] source = parseSourceFieldPath(sourceFieldPath);
            this.sourceComponent = lookupComponent(source[0]);
            checkNotNull(this.sourceComponent, "source component cannot be found");
            this.sourceFieldPath = source[1];
            this.dataDefinition = sourceComponent.getDataDefinition();
            this.sourceComponent.registerListener(path);
        } else {
            this.sourceComponent = null;
            this.sourceFieldPath = sourceFieldPath;
            this.dataDefinition = parentContainer.getDataDefinition();
        }

        registerComponent(this);
    }

    private String[] parseSourceFieldPath(final String sourceFieldPath) {
        if (sourceFieldPath.endsWith("}")) {
            return new String[] { sourceFieldPath.substring(2, sourceFieldPath.length() - 1), null };
        } else {
            String[] splittedSourceFieldPath = sourceFieldPath.split("}.");
            return new String[] { splittedSourceFieldPath[0].substring(2), splittedSourceFieldPath[1] };
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
        viewOptions.put("listeners", listeners);
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

    protected ComponentDefinition lookupComponent(final String path) {
        return parentContainter.lookupComponent(path);
    }

    protected void registerComponent(final ComponentDefinition componentDefinition) {
        parentContainter.registerComponent(componentDefinition);
    }

    protected void registerListener(final String path) {
        listeners.add(path);
    }

}
