/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ribbon.Ribbon;
import com.qcadoo.mes.view.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.ribbon.RibbonComboItem;
import com.qcadoo.mes.view.ribbon.RibbonGroup;

/**
 * Abstract implementation of {@link Component}.
 */
public abstract class AbstractComponent<T> implements Component<T> {

    private final String name;

    private final String path;

    private final String fieldPath;

    private ViewDefinition viewDefinition;

    private String sourceFieldPath;

    private Component<?> sourceComponent;

    private boolean initialized;

    private final List<ComponentOption> rawOptions = new ArrayList<ComponentOption>();

    private final Map<String, Object> options = new HashMap<String, Object>();

    private final ContainerComponent<?> parentContainer;

    private final TranslationService translationService;

    private Set<String> listeners = new HashSet<String>();

    private DataDefinition dataDefinition;

    private Ribbon ribbon;

    private boolean defaultEnabled = true;

    private boolean defaultVisible = true;

    private boolean hasDescription = false;

    /**
     * Create new {@link Component}.
     * 
     * @param name
     *            component's name
     * @param parentContainer
     *            component's parent
     * @param fieldPath
     *            field's path
     * @param sourceFieldPath
     *            sourceField's path
     * @param translationService
     *            translation service
     */
    public AbstractComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        this.name = name;
        this.parentContainer = parentContainer;
        this.fieldPath = fieldPath;
        this.translationService = translationService;

        if (parentContainer != null) {
            this.path = parentContainer.getPath() + "." + name;
            this.viewDefinition = parentContainer.getViewDefinition();
        } else {
            this.path = name;
        }

        this.sourceFieldPath = sourceFieldPath;
    }

    /**
     * @see Component#castValue(Map, JSONObject)
     */
    public abstract ViewValue<T> castComponentValue(Map<String, Entity> selectedEntities, JSONObject viewObject)
            throws JSONException;

    /**
     * @see Component#getValue(Entity, Map, ViewValue, Set, Locale)
     */
    public abstract ViewValue<T> getComponentValue(Entity entity, Entity parentEntity, Map<String, Entity> selectedEntities,
            ViewValue<T> viewValue, final Set<String> pathsToUpdate, final Locale locale);

    @Override
    public final ViewValue<T> castValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        ViewValue<T> value = castComponentValue(selectedEntities, viewObject);
        if (viewObject != null && value != null) {
            if (!viewObject.isNull("enabled")) {
                value.setEnabled(viewObject.getBoolean("enabled"));
            }
            if (!viewObject.isNull("visible")) {
                value.setVisible(viewObject.getBoolean("visible"));
            }

            if (!viewObject.isNull("updateMode")) {
                value.setUpdateMode("ignore".equals(viewObject.getString("updateMode")) ? "ignore" : "update");
            } else {
                value.setUpdateMode("update");
            }
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ViewValue<T> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<?> viewValue, final Set<String> pathsToUpdate, final Locale locale) {

        listeners = Collections.unmodifiableSet(listeners);

        Entity selectedEntity = null;
        Entity parentEntity = null;
        ViewValue<T> value = null;

        parentEntity = entity;
        if (parentEntity == null) {
            parentEntity = selectedEntities.get(getPath());
        } else if (this instanceof ContainerComponent && fieldPath != null) {
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

        if (shouldNotBeUpdated(pathsToUpdate) && !"lookupComponent".equals(getType())) {
            return null;
        }

        if (!("dynamicComboBox".equals(getType()) || "entityComboBox".equals(getType())) && !isContainer() && viewValue != null
                && viewValue.isIgnoreMode()) {
            return null;
        }

        value = getComponentValue(selectedEntity, parentEntity, selectedEntities, (ViewValue<T>) viewValue, pathsToUpdate, locale);

        if (value == null) {
            return null;
        }

        setVisibleAndEnabled(selectedEntity, value);

        return value;

    }

    private void setVisibleAndEnabled(final Entity selectedEntity, final ViewValue<T> value) {
        if (value.isEnabled() == null) {
            value.setEnabled(defaultEnabled);
        }
        if (value.isVisible() == null) {
            value.setVisible(defaultVisible);
        }

        if ((selectedEntity == null || selectedEntity.getId() == null) && (sourceComponent != null || sourceFieldPath != null)) {
            value.setEnabled(false);
        } else {
            value.setEnabled(true);
        }

        if (!defaultEnabled) {
            value.setEnabled(defaultEnabled);
        }
        if (!defaultVisible) {
            value.setEnabled(defaultVisible);
        }
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
            ((AbstractComponent<?>) sourceComponent).registerListener(path);
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

        initializeComponent();

        this.initialized = true;

        return true;
    }

    /**
     * Initialize component.
     */
    public void initializeComponent() {
        // can be implemented
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

    /**
     * Register given path as this component listener.
     */
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

    /**
     * Return true if this component can holds children components.
     */
    public boolean isContainer() {
        return false;
    }

    /**
     * Set data definition related with this component.
     * 
     * @param dataDefinition
     *            data definition
     */
    public final void setDataDefinition(final DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    /**
     * Add component's option.
     * 
     * @param option
     *            option
     */
    public final void addRawOption(final ComponentOption option) {
        rawOptions.add(option);
    }

    /**
     * Return component's options.
     * 
     * @return options
     */
    protected final List<ComponentOption> getRawOptions() {
        return rawOptions;
    }

    /**
     * Add component's option.
     * 
     * @param name
     *            name
     * @param value
     *            value
     */
    protected final void addOption(final String name, final Object value) {
        options.put(name, value);
    }

    /**
     * Return component's options.
     * 
     * @return options
     */
    public final Map<String, Object> getOptions() {
        options.put("name", name);
        options.put("listeners", listeners);
        return options;
    }

    /**
     * Return component's options as json.
     * 
     * @return options
     */
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
            if (ribbon != null) {
                jsonOptions.put("ribbon", ribbon.getAsJson());
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return jsonOptions.toString();
    }

    @Override
    public final void updateTranslations(final Map<String, String> translationsMap, final Locale locale) {
        addComponentTranslations(translationsMap, locale);
        if (this.isContainer()) {
            AbstractContainerComponent<?> container = (AbstractContainerComponent<?>) this;
            container.updateComponentsTranslations(translationsMap, locale);
        }
        addRibbonTranslations(translationsMap, locale);
    }

    private void addRibbonTranslations(final Map<String, String> translationsMap, final Locale locale) {
        if (ribbon != null) {
            for (RibbonGroup group : ribbon.getGroups()) {
                String path = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath();
                List<String> messages = Arrays.asList(new String[] { path + ".ribbon." + group.getName(),
                        "core.ribbon." + group.getName() });
                String groupTranslation = translationService.translate(messages, locale);
                translationsMap.put(path + ".ribbon." + group.getName(), groupTranslation);

                for (RibbonActionItem item : group.getItems()) {
                    addRibbonItemTranslations(translationsMap, locale, item, group.getName());
                }
            }
        }
    }

    private void addRibbonItemTranslations(final Map<String, String> translationsMap, final Locale locale,
            final RibbonActionItem item, final String ribbonPath) {
        String path = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath();
        List<String> messages = Arrays.asList(new String[] { path + ".ribbon." + ribbonPath + "." + item.getName(),
                "core.ribbon." + ribbonPath + "." + item.getName() });
        String groupTranslation = translationService.translate(messages, locale);
        translationsMap.put(path + ".ribbon." + ribbonPath + "." + item.getName(), groupTranslation);

        if (item instanceof RibbonComboItem) {
            for (RibbonActionItem subitem : ((RibbonComboItem) item).getItems()) {
                addRibbonItemTranslations(translationsMap, locale, subitem, ribbonPath + "." + item.getName());
            }
        }
    }

    /**
     * Add translations for component.
     * 
     * @param translationsMap
     *            translations
     * @param locale
     *            locale
     */
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        // can be implemented
    }

    /**
     * Return source component.
     * 
     * @return source component
     * @see AbstractComponent#AbstractComponent(String, ContainerComponent, String, String, TranslationService)
     */
    protected final Component<?> getSourceComponent() {
        return sourceComponent;
    }

    /**
     * Return parent component
     * 
     * @return parent component
     */
    protected final ContainerComponent<?> getParentContainer() {
        return parentContainer;
    }

    /**
     * Return value of the entity's field with given path. Path can contain associations.
     * 
     * @param entity
     *            entity
     * @param path
     *            path
     * @return value
     */
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

    /**
     * Return error related with the entity's field with given path. Path can contain associations.
     * 
     * @param entity
     *            entity
     * @param path
     *            path
     * @return error or null
     */
    protected final ErrorMessage getFieldError(final Entity entity, final String path) {
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

    /**
     * Return value of the entity's field with given path. Path can contain associations. Value has to be entity type (belongsTo
     * relation).
     * 
     * @param entity
     *            entity
     * @param path
     *            path
     * @return entity
     */
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

    /**
     * Return field definition. Field definition is taken using {@link #getFieldPath()} or {@link #getSourceFieldPath()}. Note
     * that not all component has associated field definitions.
     * 
     * @return field definition
     * @throws NullPointerException
     *             if component has no field definition
     */
    protected FieldDefinition getFieldDefinition() {
        String[] fields = null;

        if (getFieldPath() != null) {
            fields = getFieldPath().split("\\.");
        } else {
            fields = getSourceFieldPath().split("\\.");
        }

        DataDefinition newDataDefinition = getDataDefinition();

        if (getParentContainer() != null) {
            newDataDefinition = getParentContainer().getDataDefinition();
        }

        FieldDefinition newFieldDefinition = null;

        for (int i = 0; i < fields.length; i++) {
            FieldDefinition fieldDefinition = newDataDefinition.getField(fields[0]);
            if (fieldDefinition == null) {
                break;
            }
            if (i == fields.length - 1) {
                newFieldDefinition = fieldDefinition;
                break;
            }
            if (fieldDefinition.getType() instanceof BelongsToType) {
                newDataDefinition = ((BelongsToType) fieldDefinition.getType()).getDataDefinition();
            } else if (fieldDefinition.getType() instanceof HasManyType) {
                newDataDefinition = ((HasManyType) fieldDefinition.getType()).getDataDefinition();
            } else {
                break;
            }
        }

        checkNotNull(newFieldDefinition, "Cannot find fieldDefinitions for " + StringUtils.join(fields, ""));

        return newFieldDefinition;
    }

    /**
     * Return data definition for given path, starting from given data definition. It is used for getting data definition of the
     * associations.
     * 
     * @param dataDefinition
     *            data definition
     * @param fieldPath
     *            field's path
     * @return data definition
     */
    private DataDefinition getDataDefinitionBasedOnFieldPath(final DataDefinition dataDefinition, final String fieldPath) {
        String[] fields = fieldPath.split("\\.");

        DataDefinition newDataDefinition = dataDefinition;

        for (int i = 0; i < fields.length; i++) {
            FieldDefinition fieldDefinition = newDataDefinition.getField(fields[0]);
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
    public final String printComponent(final int tab) {
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
    public final boolean isRelatedToMainEntity() {
        return fieldPath == null && sourceComponent == null && sourceFieldPath == null;
    }

    @Override
    public final ViewDefinition getViewDefinition() {
        return viewDefinition;
    }

    /**
     * Set view definition which owns this component.
     * 
     * @param viewDefinition
     *            view definition
     */
    public final void setViewDefinition(final ViewDefinition viewDefinition) {
        this.viewDefinition = viewDefinition;
    }

    @Override
    public final boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    /**
     * Set if component should be enabled by default.
     * 
     * @param defaultEnabled
     *            default enabled
     */
    public final void setDefaultEnabled(final boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
        this.addOption("defaultEnabled", defaultEnabled);
    }

    @Override
    public final boolean isDefaultVisible() {
        return defaultVisible;
    }

    /**
     * Set if component should be visible by default.
     * 
     * @param defaultVisible
     *            default visible
     */
    public final void setDefaultVisible(final boolean defaultVisible) {
        this.defaultVisible = defaultVisible;
    }

    /**
     * Set if component has description.
     * 
     * @param hasDescription
     *            has description
     */
    public void setHasDescription(final boolean hasDescription) {
        this.hasDescription = hasDescription;
    }

    /**
     * Return true if component has description. It will be displayed close to the component label.
     * 
     * @return has description
     */
    public boolean isHasDescription() {
        return hasDescription;
    }

    /**
     * Set ribbon related with this component.
     * 
     * @param ribbon
     *            ribbon
     */
    public final void setRibbon(final Ribbon ribbon) {
        this.ribbon = ribbon;
    }

    /**
     * Return translation service.
     * 
     * @return translation service
     */
    protected final TranslationService getTranslationService() {
        return translationService;
    }

    /**
     * Find value of the component with {@link #getPath()} starting with given value.
     * 
     * @param viewValue
     *            starting view value
     * @return value
     */
    protected ViewValue<?> lookupViewValue(final ViewValue<Long> viewValue) {
        ViewValue<?> lookupedViewValue = viewValue;
        String[] fields = getPath().split("\\.");

        for (String field : fields) {
            lookupedViewValue = lookupedViewValue.getComponent(field);
            if (lookupedViewValue == null) {
                return null;
            }
        }

        return lookupedViewValue;

    }

}
