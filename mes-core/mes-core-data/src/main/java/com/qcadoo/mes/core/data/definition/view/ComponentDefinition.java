package com.qcadoo.mes.core.data.definition.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.internal.types.BelongsToType;
import com.qcadoo.mes.core.data.internal.types.HasManyType;

public abstract class ComponentDefinition {

    private final String name;

    private final String path;

    private final String fieldPath;

    private String sourceFieldPath;

    private ComponentDefinition sourceComponent;

    private boolean initialized;

    private final Map<String, String> options = new HashMap<String, String>();

    private final ContainerDefinition parentContainer;

    private final Set<String> listeners = new HashSet<String>();

    private DataDefinition dataDefinition;

    public abstract String getType();

    public abstract Object getValue(Entity entity, Map<String, Object> selectableValues, Object viewEntity);

    protected String getFieldValue(final Entity entity) {
        if (fieldPath != null) {
            String[] fields = fieldPath.split("\\.");
            Object value = entity;

            for (String field : fields) {
                if (value instanceof Entity) {
                    value = entity.getField(field);
                } else {
                    return null;
                }
            }

            return String.valueOf(value);
        }

        return null;
    }

    public ComponentDefinition(final String name, final ContainerDefinition parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        this.name = name;
        this.parentContainer = parentContainer;
        this.fieldPath = fieldPath;

        if (parentContainer != null) {
            this.path = parentContainer.getPath() + "." + name;
        } else {
            this.path = name;
        }

        this.sourceFieldPath = sourceFieldPath;
    }

    public boolean initializeComponent(final Map<String, ComponentDefinition> componentRegistry) {
        if (sourceFieldPath != null && sourceFieldPath.startsWith("#")) {
            String[] source = parseSourceFieldPath(sourceFieldPath);
            sourceComponent = componentRegistry.get(source[0]);

            if (sourceComponent == null || !sourceComponent.isInitialized()) {
                return false;
            }

            sourceFieldPath = source[1];
            dataDefinition = sourceComponent.getDataDefinition();
            sourceComponent.registerListener(path);
        } else if (parentContainer != null) {
            if (!parentContainer.isInitialized()) {
                return false;
            }
            sourceComponent = null;
            dataDefinition = parentContainer.getDataDefinition();
        } else {
            sourceComponent = null;
            dataDefinition = null;
        }

        if (sourceFieldPath != null) {
            dataDefinition = getDataDefinitionBasedOnFieldPath(dataDefinition, sourceFieldPath);
        } else if (fieldPath != null) {
            dataDefinition = getDataDefinitionBasedOnFieldPath(dataDefinition, fieldPath);
        }

        this.initialized = true;

        return true;
    }

    private DataDefinition getDataDefinitionBasedOnFieldPath(final DataDefinition dataDefinition, final String fieldPath) {
        String[] fields = fieldPath.split("\\.");

        DataDefinition newDataDefinition = dataDefinition;

        for (String field : fields) {
            DataFieldDefinition fieldDefinition = newDataDefinition.getField(field);
            if (fieldDefinition == null) {
                break;
            }
            if (fieldDefinition.getType() instanceof BelongsToType) {
                newDataDefinition = ((BelongsToType) fieldDefinition.getType()).getDataDefinition();
            } else if (fieldDefinition.getType() instanceof HasManyType) {
                newDataDefinition = ((HasManyType) fieldDefinition.getType()).getDataDefinition();
            }
        }

        return newDataDefinition;
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
        return parentContainer;
    }

    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    public void setDataDefinition(final DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    public void addOptions(final String name, final String value) {
        this.options.put(name, value);
    }

    protected void registerListener(final String path) {
        listeners.add(path);
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public String toString() {
        String dd = dataDefinition != null ? dataDefinition.getName() : "null";
        String sc = sourceComponent != null ? sourceComponent.getPath() : "null";
        return path + ", [" + fieldPath + ", " + sourceFieldPath + ", " + sc + "], [" + listeners + "], " + dd;
    }

}
