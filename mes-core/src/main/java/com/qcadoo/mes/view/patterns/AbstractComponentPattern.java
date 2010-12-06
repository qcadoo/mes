package com.qcadoo.mes.view.patterns;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.ScopeEntityIdChangeListener;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.states.AbstractComponentState;

public abstract class AbstractComponentPattern implements ComponentPattern {

    protected static final String JSP_PATH = "AbstractJspPath";

    protected static final String JS_PATH = "AbstractJavaScriptPath";

    protected static final String JS_OBJECT = "AbstractJavascriptObject";

    private final String name;

    private final String fieldPath;

    private final String scopeFieldPath;

    private final ComponentPattern parent;

    private final boolean defaultEnabled;

    private final boolean defaultVisible;

    private final boolean hasDescription;

    private final String reference;

    private final TranslationService translationService;

    private final ViewDefinition viewDefinition;

    private final Map<String, ComponentPattern> fieldEntityIdChangeListeners = new HashMap<String, ComponentPattern>();

    private final Map<String, ComponentPattern> scopeEntityIdChangeListeners = new HashMap<String, ComponentPattern>();

    private final List<ComponentOption> options = new ArrayList<ComponentOption>();

    private FieldDefinition fieldDefinition;

    private FieldDefinition scopeFieldDefinition;

    private DataDefinition dataDefinition;

    private boolean initialized;

    public AbstractComponentPattern(final ComponentDefinition componentDefinition) {
        checkArgument(hasText(componentDefinition.getName()), "Component name must be specified");
        this.name = componentDefinition.getName();
        this.fieldPath = componentDefinition.getFieldPath();
        this.scopeFieldPath = componentDefinition.getSourceFieldPath();
        this.parent = componentDefinition.getParent();
        this.reference = componentDefinition.getReference();
        this.hasDescription = componentDefinition.isHasDescription();
        this.defaultEnabled = componentDefinition.isDefaultEnabled();
        this.defaultVisible = componentDefinition.isDefaultVisible();
        this.translationService = componentDefinition.getTranslationService();
        this.viewDefinition = componentDefinition.getViewDefinition();
    }

    protected abstract String getJspFilePath();

    protected abstract String getJsFilePath();

    protected abstract String getJsObjectName();

    protected abstract ComponentState getComponentStateInstance();

    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        // reimplement me if you want
        return new JSONObject();
    }

    protected void initializeComponent() throws JSONException {
        // implement me if you want
    }

    @Override
    public ComponentState createComponentState() {
        AbstractComponentState state = (AbstractComponentState) getComponentStateInstance();
        state.setDataDefinition(dataDefinition);
        state.setName(name);
        state.setEnabled(isDefaultEnabled());
        state.setVisible(isDefaultVisible());
        state.setTranslationService(translationService);
        state.setTranslationPath(getTranslationPath());
        return state;
    }

    @Override
    public Map<String, Object> prepareView(final Locale locale) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", getName());
        map.put("path", getPath());
        map.put("jspFilePath", getJspFilePath());
        map.put("jsFilePath", getJsFilePath());
        map.put("jsObjectName", getJsObjectName());

        try {
            JSONObject jsOptions = getJsOptions(locale);
            addListenersToJsOptions(jsOptions);
            map.put("jsOptions", jsOptions);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        return map;
    }

    protected void prepareComponentView(final Map<String, Object> map, final Locale locale) throws JSONException {
        // implement me if you want
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getPath() {
        if (parent == null) {
            return name;
        } else {
            return parent.getPath() + "." + name;
        }
    }

    @Override
    public final boolean initialize() {
        if (initialized) {
            return true;
        }

        viewDefinition.addJsFilePath(getJsFilePath());

        String[] field = null;
        String[] scopeField = null;
        AbstractComponentPattern fieldComponent = null;
        AbstractComponentPattern scopeFieldComponent = null;

        if (fieldPath != null) {
            field = getComponentAndField(fieldPath);
            fieldComponent = (AbstractComponentPattern) (field[0] == null ? parent : viewDefinition.getComponentByPath(field[0]));
            checkNotNull(fieldComponent, "Cannot find field component for " + getPath() + ": " + fieldPath);
            fieldComponent.addFieldEntityIdChangeListener(field[1], this);
        }

        if (scopeFieldPath != null) {
            scopeField = getComponentAndField(scopeFieldPath);
            scopeFieldComponent = (AbstractComponentPattern) (scopeField[0] == null ? parent : viewDefinition
                    .getComponentByPath(scopeField[0]));
            checkNotNull(scopeFieldComponent, "Cannot find sourceField component for " + getPath() + ": " + scopeFieldPath);
            scopeFieldComponent.addScopeEntityIdChangeListener(scopeField[1], this);
        }

        if (isComponentInitialized(fieldComponent) && isComponentInitialized(scopeFieldComponent)) {
            initialized = true;
        } else {
            return false;
        }

        getDataDefinition(viewDefinition, fieldComponent, scopeFieldComponent);

        getFieldAndScopeFieldDefinitions(field, scopeField);

        getDataDefinitionFromFieldDefinition();

        try {
            initializeComponent();
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        return true;
    }

    public final void addFieldEntityIdChangeListener(final String field, final ComponentPattern listener) {
        fieldEntityIdChangeListeners.put(field, listener);
    }

    public final void addScopeEntityIdChangeListener(final String field, final ComponentPattern listener) {
        scopeEntityIdChangeListeners.put(field, listener);
    }

    public void updateComponentStateListeners(final ViewDefinitionState viewDefinitionState) {
        // TODO masz?
        if (fieldEntityIdChangeListeners.size() > 0) {
            AbstractComponentState thisComponentState = (AbstractComponentState) viewDefinitionState
                    .getComponentByPath(getPath());
            for (Map.Entry<String, ComponentPattern> listenerPattern : fieldEntityIdChangeListeners.entrySet()) {
                ComponentState listenerState = viewDefinitionState.getComponentByPath(listenerPattern.getValue().getPath());
                thisComponentState.addFieldEntityIdChangeListener(listenerPattern.getKey(),
                        (FieldEntityIdChangeListener) listenerState);
            }
        }
        if (scopeEntityIdChangeListeners.size() > 0) {
            AbstractComponentState thisComponentState = (AbstractComponentState) viewDefinitionState
                    .getComponentByPath(getPath());
            for (Map.Entry<String, ComponentPattern> listenerPattern : scopeEntityIdChangeListeners.entrySet()) {
                ComponentState listenerState = viewDefinitionState.getComponentByPath(listenerPattern.getValue().getPath());
                thisComponentState.addScopeEntityIdChangeListener(listenerPattern.getKey(),
                        (ScopeEntityIdChangeListener) listenerState);
            }
        }
    }

    protected final Map<String, ComponentPattern> getFieldEntityIdChangeListeners() {
        return fieldEntityIdChangeListeners;
    }

    protected final Map<String, ComponentPattern> getScopeEntityIdChangeListeners() {
        return scopeEntityIdChangeListeners;
    }

    protected final boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    protected final boolean isDefaultVisible() {
        return defaultVisible;
    }

    protected final boolean isHasDescription() {
        return hasDescription;
    }

    public final void addOption(final ComponentOption option) {
        options.add(option);
    }

    protected final List<ComponentOption> getOptions() {
        return options;
    }

    protected final String getReference() {
        return reference;
    }

    protected final FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    protected final FieldDefinition getScopeFieldDefinition() {
        return scopeFieldDefinition;
    }

    protected final DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    protected final TranslationService getTranslationService() {
        return translationService;
    }

    protected final ViewDefinition getViewDefinition() {
        return viewDefinition;
    }

    protected final String getTranslationPath() {
        return getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath();
    }

    private void addListenersToJsOptions(final JSONObject jsOptions) throws JSONException {
        JSONArray listeners = new JSONArray();
        if (fieldEntityIdChangeListeners.size() > 0 || scopeEntityIdChangeListeners.size() > 0) {
            for (ComponentPattern listener : fieldEntityIdChangeListeners.values()) {
                listeners.put(listener.getPath());
            }
            for (ComponentPattern listener : scopeEntityIdChangeListeners.values()) {
                listeners.put(listener.getPath());
            }
        }
        jsOptions.put("listeners", listeners);
    }

    private boolean isComponentInitialized(final AbstractComponentPattern fieldComponent) {
        return fieldComponent == null || fieldComponent.initialized;
    }

    private void getDataDefinitionFromFieldDefinition() {
        if (fieldDefinition != null) {
            getDataDefinitionFromFieldDefinition(fieldDefinition);
        } else if (scopeFieldDefinition != null) {
            getDataDefinitionFromFieldDefinition(scopeFieldDefinition);
        }
    }

    private void getFieldAndScopeFieldDefinitions(final String[] field, final String[] scopeField) {
        if (dataDefinition != null) {
            if (fieldPath != null) {
                fieldDefinition = dataDefinition.getField(field[1]);
            }

            if (scopeFieldPath != null) {
                scopeFieldDefinition = dataDefinition.getField(scopeField[1]);
            }
        }
    }

    private void getDataDefinition(final ViewDefinition viewDefinition, final AbstractComponentPattern fieldComponent,
            final AbstractComponentPattern scopeFieldComponent) {
        if (fieldPath != null) {
            dataDefinition = fieldComponent.getDataDefinition();
        } else if (scopeFieldPath != null) {
            dataDefinition = scopeFieldComponent.getDataDefinition();
        } else if (parent != null) {
            dataDefinition = ((AbstractComponentPattern) parent).getDataDefinition();
        } else {
            dataDefinition = viewDefinition.getDataDefinition();
        }
    }

    private void getDataDefinitionFromFieldDefinition(final FieldDefinition fieldDefinition) {
        if (fieldDefinition.getType() instanceof HasManyType) {
            dataDefinition = ((HasManyType) fieldDefinition.getType()).getDataDefinition();
        } else if (fieldDefinition.getType() instanceof BelongsToType) {
            dataDefinition = ((BelongsToType) fieldDefinition.getType()).getDataDefinition();
        }
    }

    private String[] getComponentAndField(final String path) {
        Pattern p = Pattern.compile("^#\\{.+\\}\\.");
        Matcher m = p.matcher(path);
        if (m.find()) {
            String field = path.substring(m.end());
            String component = path.substring(2, m.end() - 2);
            return new String[] { component, field };
        } else {
            return new String[] { null, path };
        }
    }

}