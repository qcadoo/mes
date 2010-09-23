package com.qcadoo.mes.core.data.view;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.internal.types.BelongsToType;
import com.qcadoo.mes.core.data.internal.types.HasManyType;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.model.HookDefinition;
import com.qcadoo.mes.core.data.validation.ValidationError;

public abstract class AbstractComponent<T> implements Component<T> {

    private final String name;

    private final String path;

    private final String fieldPath;

    private String viewName;

    private String sourceFieldPath;

    private Component<?> sourceComponent;

    private boolean initialized;

    private final Map<String, String> options = new HashMap<String, String>();

    private final ContainerComponent<?> parentContainer;

    private Set<String> listeners = new HashSet<String>();

    private DataDefinition dataDefinition;

    private HookDefinition hookDefinition;

    public AbstractComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        this.name = name;
        this.parentContainer = parentContainer;
        this.fieldPath = fieldPath;

        if (parentContainer != null) {
            this.path = parentContainer.getPath() + "." + name;
            this.viewName = parentContainer.getViewName();
        } else {
            this.path = name;
        }

        this.sourceFieldPath = sourceFieldPath;
    }

    public abstract ViewValue<T> castComponentValue(Map<String, Entity> selectedEntities, JSONObject viewObject)
            throws JSONException;

    public abstract ViewValue<T> getComponentValue(Entity entity, Entity parentEntity, Map<String, Entity> selectedEntities,
            ViewValue<T> viewValue, final Set<String> pathsToUpdate);

    @Override
    public final ViewValue<T> castValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        ViewValue<T> value = castComponentValue(selectedEntities, viewObject);
        if (viewObject != null && value != null) {
            value.setEnabled(viewObject.getBoolean("enabled"));
            value.setVisible(viewObject.getBoolean("visible"));
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ViewValue<T> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<?> viewValue, final Set<String> pathsToUpdate) {

        listeners = Collections.unmodifiableSet(listeners);

        if (shouldNotBeUpdated(pathsToUpdate)) {
            return (ViewValue<T>) viewValue;
        }

        Entity selectedEntity = null;
        Entity parentEntity = null;
        ViewValue<T> value = null;

        parentEntity = entity;
        if (parentEntity == null) {
            parentEntity = selectedEntities.get(getPath());
        } else if (this instanceof ContainerComponent && entity != null && fieldPath != null) {
            parentEntity = getFieldEntityValue(entity, fieldPath);
        }

        if (sourceComponent != null) {
            selectedEntity = selectedEntities.get(sourceComponent.getPath());

            if (this instanceof ContainerComponent && selectedEntity != null && sourceFieldPath != null) {
                selectedEntity = getFieldEntityValue(selectedEntity, sourceFieldPath);
            }

        } else {
            selectedEntity = parentEntity;
        }

        // if (sourceComponent != null) {
        // selectedEntity = selectedEntities.get(sourceComponent.getPath());
        //
        // if (this instanceof ContainerComponent && selectedEntity != null && sourceFieldPath != null) {
        // selectedEntity = getFieldEntityValue(selectedEntity, sourceFieldPath);
        // }
        //
        // } else {
        // selectedEntity = entity;
        //
        // if (selectedEntity == null) {
        // selectedEntity = selectedEntities.get(getPath());
        // } else if (this instanceof ContainerComponent && entity != null && fieldPath != null) {
        // selectedEntity = getFieldEntityValue(entity, fieldPath);
        // }
        // }

        value = getComponentValue(selectedEntity, parentEntity, selectedEntities, (ViewValue<T>) viewValue, pathsToUpdate);

        if (selectedEntity == null && (sourceComponent != null || sourceFieldPath != null)) {
            value.setEnabled(false);
        }

        return value;

    }

    @Override
    public final boolean initializeComponent(final Map<String, Component<?>> componentRegistry) {
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

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getSourceFieldPath() {
        return sourceFieldPath;
    }

    @Override
    public final String getPath() {
        return path;
    }

    @Override
    public final String getFieldPath() {
        return fieldPath;
    }

    @Override
    public final DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    public HookDefinition getHookDefinition() {
        return hookDefinition;
    }

    @Override
    public final void registerListener(final String path) {
        listeners.add(path);
    }

    @Override
    public final Set<String> getListeners() {
        return listeners;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    public boolean isContainer() {
        return false;
    }

    public final void setDataDefinition(final DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    public final void addOptions(final String name, final String value) {
        this.options.put(name, value);
    }

    public final Map<String, Object> getOptions() {
        Map<String, Object> viewOptions = new HashMap<String, Object>(options);
        viewOptions.put("name", name);
        viewOptions.put("listeners", listeners);
        addComponentOptions(viewOptions);
        return viewOptions;
    }

    public void addComponentOptions(final Map<String, Object> viewOptions) {
        // can be implemented
    }

    public final String getOptionsAsJson() {
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
            throw new RuntimeException(e.getMessage(), e);
        }
        return jsonOptions.toString();
    }

    @Override
    public final void updateTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        addComponentTranslations(translationsMap, translationService, locale);
        if (this.isContainer()) {
            AbstractContainerComponent<?> container = (AbstractContainerComponent<?>) this;
            container.updateComponentsTranslations(translationsMap, translationService, locale);
        }
    }

    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        // can be implemented
    }

    protected final Component<?> getSourceComponent() {
        return sourceComponent;
    }

    protected final ContainerComponent<?> getParentContainer() {
        return parentContainer;
    }

    protected final Object getFieldValue(final Entity entity, final String path) {
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

    protected final ValidationError getFieldError(final Entity entity, final String path) {
        if (entity == null || path == null) {
            return null;
        }

        String[] fields = path.split("\\.");
        Object value = entity;

        for (int i = 0; i < fields.length - 1; i++) {
            value = ((Entity) value).getField(fields[i]);
            if (!(value instanceof Entity)) {
                return null;
            }
        }

        if (fields.length > 0) {
            return ((Entity) value).getError(fields[fields.length - 1]);
        } else {
            return null;
        }
    }

    private Entity getFieldEntityValue(final Entity entity, final String path) {
        Object value = getFieldValue(entity, path);

        if (value == null) {
            return null;
        } else if (value instanceof Entity) {
            return (Entity) value;
        } else {
            throw new IllegalStateException("Field " + path + " should has Entity type");
        }
    }

    private DataDefinition getDataDefinitionBasedOnFieldPath(final DataDefinition dataDefinition, final String fieldPath) {
        String[] fields = fieldPath.split("\\.");

        DataDefinition newDataDefinition = dataDefinition;

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

    private boolean shouldNotBeUpdated(final Set<String> pathsToUpdate) {
        if (pathsToUpdate == null || pathsToUpdate.isEmpty()) {
            return false;
        }
        for (String pathToUpdate : pathsToUpdate) {
            if (getPath().startsWith(pathToUpdate)) {
                return false;
            }
            if (this instanceof ContainerComponent && pathToUpdate.startsWith(getPath())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final String toString() {
        return printComponent(0);
    }

    @SuppressWarnings("rawtypes")
    public final String printComponent(int tab) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tab; i++) {
            sb.append("    ");
        }
        String dd = dataDefinition != null ? dataDefinition.getName() : "null";
        String sc = sourceComponent != null ? sourceComponent.getPath() : "null";
        sb.append(path + ", [" + fieldPath + ", " + sourceFieldPath + ", " + sc + "], [" + listeners + "], " + dd + "\n");
        if (isContainer()) {
            AbstractContainerComponent container = (AbstractContainerComponent) this;
            for (Object co : container.getComponents().values()) {
                sb.append(((AbstractComponent) co).printComponent(tab + 1));
            }
        }
        return sb.toString();
    }

    @Override
    public String getViewName() {
        return viewName;
    }

    public void setViewName(final String viewName) {
        this.viewName = viewName;
    }

}
