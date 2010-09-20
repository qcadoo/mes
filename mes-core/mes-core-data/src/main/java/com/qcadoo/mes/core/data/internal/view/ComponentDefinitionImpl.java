package com.qcadoo.mes.core.data.internal.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.types.BelongsToType;
import com.qcadoo.mes.core.data.internal.types.HasManyType;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.view.ComponentDefinition;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewEntity;

public abstract class ComponentDefinitionImpl<T> implements ComponentDefinition<T> {

    private final String name;

    private final String path;

    private final String fieldPath;

    private String sourceFieldPath;

    private ComponentDefinition<?> sourceComponent;

    private boolean initialized;

    private final Map<String, String> options = new HashMap<String, String>();

    private final ContainerComponent<?> parentContainer;

    private final Set<String> listeners = new HashSet<String>();

    private ModelDefinition dataDefinition;

    @Override
    public abstract String getType();

    public abstract ViewEntity<T> castComponentValue(Entity entity, Map<String, Entity> selectedEntities, JSONObject viewObject)
            throws JSONException;

    @Override
    public final ViewEntity<T> castValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        ViewEntity<T> value = castComponentValue(entity, selectedEntities, viewObject);
        if (viewObject != null && value != null) {
            value.setEnabled(viewObject.getBoolean("enabled"));
            value.setVisible(viewObject.getBoolean("visible"));
        }
        return value;
    }

    public abstract ViewEntity<T> getComponentValue(Entity entity, Map<String, Entity> selectedEntities,
            ViewEntity<T> viewEntity, final Set<String> pathsToUpdate);

    @Override
    @SuppressWarnings("unchecked")
    public ViewEntity<T> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewEntity<?> viewEntity, final Set<String> pathsToUpdate) {
        if (shouldNotBeUpdated(pathsToUpdate)) {
            return null;
        }
        if (sourceComponent != null) {
            Entity selectedEntity = selectedEntities.get(sourceComponent.getPath());

            if (this instanceof ContainerDefinitionImpl && selectedEntity != null && sourceFieldPath != null) {
                selectedEntity = getFieldEntityValue(selectedEntity, sourceFieldPath);
            }

            return getComponentValue(selectedEntity, selectedEntities, (ViewEntity<T>) viewEntity, pathsToUpdate);
        } else {
            Entity contextEntity = entity;
            if (this instanceof ContainerDefinitionImpl && entity != null && fieldPath != null) {
                contextEntity = getFieldEntityValue(entity, fieldPath);
            }

            return getComponentValue(contextEntity, selectedEntities, (ViewEntity<T>) viewEntity, pathsToUpdate);
        }
    }

    private boolean shouldNotBeUpdated(final Set<String> pathsToUpdate) {
        if (pathsToUpdate == null || pathsToUpdate.isEmpty()) {
            return false;
        }
        for (String path : pathsToUpdate) {
            if (getPath().startsWith(path)) {
                return false;
            }
            if (this instanceof ContainerDefinitionImpl && path.startsWith(getPath())) {
                return false;
            }
        }
        return true;
    }

    protected String getFieldStringValue(final Entity entity, final Map<String, Entity> selectedEntities) {
        Object value = null;

        if (getSourceComponent() != null) {
            value = getFieldValue(selectedEntities.get(getSourceComponent().getPath()), getSourceFieldPath());
        } else {
            value = getFieldValue(entity, getFieldPath());
        }

        if (value == null) {
            return "";
        } else {
            return String.valueOf(value);
        }
    }

    protected Entity getFieldEntityValue(final Entity entity, final String path) {
        Object value = getFieldValue(entity, path);

        if (value == null) {
            return null;
        } else if (value instanceof Entity) {
            return (Entity) value;
        } else {
            throw new IllegalStateException("Field " + path + " should has Entity type");
        }
    }

    protected Object getFieldValue(final Entity entity, final String path) {
        if (entity == null || path == null) {
            return null;
        }

        String[] fields = path.split("\\.");
        Object value = entity;

        for (String field : fields) {
            if (value instanceof Entity) {
                value = ((Entity) value).getField(field);
            } else {
                return null;
            }
        }

        return value;
    }

    public ComponentDefinitionImpl(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
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

    @Override
    public boolean initializeComponent(final Map<String, ComponentDefinition<?>> componentRegistry) {
        if (sourceFieldPath != null && sourceFieldPath.startsWith("#")) {
            String[] source = parseSourceFieldPath(sourceFieldPath);
            sourceComponent = componentRegistry.get(source[0]);

            if (sourceComponent == null || !sourceComponent.isInitialized()) {
                return false;
            }

            sourceFieldPath = source[1];
            dataDefinition = sourceComponent.getModelDefinition();
            sourceComponent.registerListener(path);
        } else if (parentContainer != null) {
            if (!parentContainer.isInitialized()) {
                return false;
            }
            sourceComponent = null;
            dataDefinition = parentContainer.getModelDefinition();
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

    private ModelDefinition getDataDefinitionBasedOnFieldPath(final ModelDefinition dataDefinition, final String fieldPath) {
        String[] fields = fieldPath.split("\\.");

        ModelDefinition newDataDefinition = dataDefinition;

        for (String field : fields) {
            FieldDefinition fieldDefinition = newDataDefinition.getField(field);
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSourceFieldPath() {
        return sourceFieldPath;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getFieldPath() {
        return fieldPath;
    }

    public Map<String, Object> getOptions() {
        Map<String, Object> viewOptions = new HashMap<String, Object>(options);
        viewOptions.put("name", name);
        viewOptions.put("listeners", listeners);
        return viewOptions;
    }

    public String getOptionsAsJson() {
        JSONObject jsonOptions = new JSONObject();
        try {
            for (Entry<String, Object> option : getOptions().entrySet()) {
                Object value = null;
                if (option.getValue() instanceof Collection) {
                    Collection<?> list = (Collection<?>) option.getValue();
                    JSONArray arr = new JSONArray();
                    for (Object o : list) {
                        arr.put(o);
                    }
                    value = arr;
                } else {
                    jsonOptions.put(option.getKey(), option.getValue());
                    value = option.getValue();
                }
                if (value != null) {
                    jsonOptions.put(option.getKey(), option.getValue());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonOptions.toString();
    }

    protected ComponentDefinition<?> getSourceComponent() {
        return sourceComponent;
    }

    protected ContainerComponent<?> getParentContainer() {
        return parentContainer;
    }

    @Override
    public ModelDefinition getModelDefinition() {
        return dataDefinition;
    }

    public void setDataDefinition(final ModelDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    public void addOptions(final String name, final String value) {
        this.options.put(name, value);
    }

    @Override
    public void registerListener(final String path) {
        listeners.add(path);
    }

    @Override
    public Set<String> getListeners() {
        return listeners;
    }

    @Override
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
