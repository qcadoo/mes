package com.qcadoo.mes.view.patterns;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private final Map<String, ComponentPattern> fieldEntityIdChangeListeners = new HashMap<String, ComponentPattern>();

    private final Map<String, ComponentPattern> scopeEntityIdChangeListeners = new HashMap<String, ComponentPattern>();

    private FieldDefinition fieldDefinition;

    private FieldDefinition scopeFieldDefinition;

    private DataDefinition dataDefinition;

    private boolean initialized;

    private boolean defaultEnabled;

    private boolean defaultVisible;

    private boolean hasDescription;

    private String reference;

    private final List<ComponentOption> options = new ArrayList<ComponentOption>();

    private TranslationService translationService;

    private final JSONObject jsOptions = new JSONObject();

    public AbstractComponentPattern(final String name, final String fieldPath, final String scopeFieldPath,
            final ComponentPattern parent) {
        checkArgument(hasText(name), "Name must be specified");
        this.name = name;
        this.fieldPath = fieldPath;
        this.scopeFieldPath = scopeFieldPath;
        this.parent = parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPathName() {
        if (parent == null) {
            return name;
        } else {
            return parent.getPathName() + "." + name;
        }
    }

    @Override
    public boolean initialize(final ViewDefinition viewDefinition) {
        if (initialized) {
            return true;
        }

        System.out.println("#1 ---> " + getPathName());

        String[] field = null;
        String[] scopeField = null;
        AbstractComponentPattern fieldComponent = null;
        AbstractComponentPattern scopeFieldComponent = null;

        if (fieldPath != null) {
            System.out.println("#2 ---> " + getPathName() + ": " + fieldPath);
            field = getComponentAndField(fieldPath);
            System.out.println("#2.1 ---> " + getPathName() + ": " + field[0]);
            System.out.println("#2.2 ---> " + getPathName() + ": " + field[1]);
            fieldComponent = (AbstractComponentPattern) (field[0] == null ? parent : viewDefinition.getComponentByPath(field[0]));
            System.out.println("#2.3 ---> " + getPathName() + ": " + fieldComponent);
            fieldComponent.addFieldEntityIdChangeListener(field[1], this);
        }

        if (scopeFieldPath != null) {
            System.out.println("#3 ---> " + getPathName() + ": " + scopeFieldPath);
            scopeField = getComponentAndField(scopeFieldPath);
            System.out.println("#3.1 ---> " + getPathName() + ": " + scopeField[0]);
            System.out.println("#3.2 ---> " + getPathName() + ": " + scopeField[1]);
            scopeFieldComponent = (AbstractComponentPattern) (scopeField[0] == null ? parent : viewDefinition
                    .getComponentByPath(scopeField[0]));
            System.out.println("#3.3 ---> " + getPathName() + ": " + scopeFieldComponent);
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
            initializeOptions();
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        return true;
    }

    protected void initializeOptions() throws JSONException {
        // implement me if you want
    }

    private boolean isComponentInitialized(final AbstractComponentPattern fieldComponent) {
        System.out.println("#9.1 ---> " + getPathName() + ", " + fieldComponent);
        if (fieldComponent != null) {
            System.out.println("#9.2 ---> " + getPathName() + ", " + fieldComponent.initialized);
        }
        return fieldComponent == null || fieldComponent.initialized;
    }

    private void getDataDefinitionFromFieldDefinition() {
        if (fieldDefinition != null) {
            System.out.println("#6.1 ---> " + getPathName());
            getDataDefinitionFromFieldDefinition(fieldDefinition);
        } else if (scopeFieldDefinition != null) {
            System.out.println("#6.2 ---> " + getPathName());
            getDataDefinitionFromFieldDefinition(scopeFieldDefinition);
        }
    }

    private void getFieldAndScopeFieldDefinitions(final String[] field, final String[] scopeField) {
        if (dataDefinition != null) {
            if (fieldPath != null) {
                fieldDefinition = dataDefinition.getField(field[1]);
                System.out.println("#5.1 ---> " + getPathName() + ": " + dataDefinition + ", " + field[1] + ", "
                        + fieldDefinition);
            }

            if (scopeFieldPath != null) {
                scopeFieldDefinition = dataDefinition.getField(scopeField[1]);
                System.out.println("#5.2 ---> " + getPathName() + ": " + dataDefinition + ", " + scopeField[1] + ", "
                        + scopeFieldDefinition);
            }
        }
    }

    private void getDataDefinition(final ViewDefinition viewDefinition, final AbstractComponentPattern fieldComponent,
            final AbstractComponentPattern scopeFieldComponent) {
        if (fieldPath != null) {
            dataDefinition = fieldComponent.getDataDefinition();
            System.out.println("#4.1 ---> " + getPathName() + ": " + dataDefinition);
        } else if (scopeFieldPath != null) {
            dataDefinition = scopeFieldComponent.getDataDefinition();
            System.out.println("#4.2 ---> " + getPathName() + ": " + dataDefinition);
        } else if (parent != null) {
            dataDefinition = ((AbstractComponentPattern) parent).getDataDefinition();
            System.out.println("#4.3 ---> " + getPathName() + ": " + dataDefinition);
        } else {
            dataDefinition = viewDefinition.getDataDefinition();
            System.out.println("#4.4 ---> " + getPathName() + ": " + dataDefinition);
        }
    }

    private void getDataDefinitionFromFieldDefinition(final FieldDefinition fieldDefinition) {
        System.out.println("#7.1 ---> " + getPathName() + ": " + fieldDefinition);
        if (fieldDefinition.getType() instanceof HasManyType) {
            dataDefinition = ((HasManyType) fieldDefinition.getType()).getDataDefinition();
            System.out.println("#7.2 ---> " + getPathName() + ": " + dataDefinition);
        } else if (fieldDefinition.getType() instanceof BelongsToType) {
            dataDefinition = ((BelongsToType) fieldDefinition.getType()).getDataDefinition();
            System.out.println("#7.3 ---> " + getPathName() + ": " + dataDefinition);
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

    public void addFieldEntityIdChangeListener(final String field, final ComponentPattern listener) {
        fieldEntityIdChangeListeners.put(field, listener);
    }

    public void addScopeEntityIdChangeListener(final String field, final ComponentPattern listener) {
        scopeEntityIdChangeListeners.put(field, listener);
    }

    protected Map<String, ComponentPattern> getFieldEntityIdChangeListeners() {
        return fieldEntityIdChangeListeners;
    }

    protected Map<String, ComponentPattern> getScopeEntityIdChangeListeners() {
        return scopeEntityIdChangeListeners;
    }

    public abstract ComponentState getComponentStateInstance();

    @Override
    public ComponentState createComponentState() {
        AbstractComponentState state = (AbstractComponentState) getComponentStateInstance();
        state.setDataDefinition(dataDefinition);
        state.setName(name);
        state.setEnabled(isDefaultEnabled());
        state.setVisible(isDefaultVisible());
        state.setTranslationService(translationService);
        return state;
    }

    public void setTranslationService(final TranslationService translationService) {
        this.translationService = translationService;
    }

    public void updateComponentStateListeners(final ViewDefinitionState viewDefinitionState) {
        if (fieldEntityIdChangeListeners.size() > 0) {
            AbstractComponentState thisComponentState = (AbstractComponentState) viewDefinitionState
                    .getComponentByPath(getPathName());
            for (Map.Entry<String, ComponentPattern> listenerPattern : fieldEntityIdChangeListeners.entrySet()) {
                ComponentState listenerState = viewDefinitionState.getComponentByPath(listenerPattern.getValue().getPathName());
                thisComponentState.addFieldEntityIdChangeListener(listenerPattern.getKey(),
                        (FieldEntityIdChangeListener) listenerState);
            }
        }
        if (scopeEntityIdChangeListeners.size() > 0) {
            AbstractComponentState thisComponentState = (AbstractComponentState) viewDefinitionState
                    .getComponentByPath(getPathName());
            for (Map.Entry<String, ComponentPattern> listenerPattern : scopeEntityIdChangeListeners.entrySet()) {
                ComponentState listenerState = viewDefinitionState.getComponentByPath(listenerPattern.getValue().getPathName());
                thisComponentState.addScopeEntityIdChangeListener(listenerPattern.getKey(),
                        (ScopeEntityIdChangeListener) listenerState);
            }
        }
    }

    public void setDefaultEnabled(final boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public void setDefaultVisible(final boolean defaultVisible) {
        this.defaultVisible = defaultVisible;
    }

    public boolean isDefaultVisible() {
        return defaultVisible;
    }

    public void setHasDescription(final boolean hasDescription) {
        this.hasDescription = hasDescription;
    }

    public boolean isHasDescription() {
        return hasDescription;
    }

    public void addOption(final ComponentOption option) {
        options.add(option);
    }

    protected List<ComponentOption> getOptions() {
        return options;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    protected FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    protected FieldDefinition getScopeFieldDefinition() {
        return scopeFieldDefinition;
    }

    protected DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    protected final void addStaticJavaScriptOption(final String optionName, final Object optionValue) {
        try {
            jsOptions.put(optionName, optionValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final JSONObject getStaticJavaScriptOptions() {
        try {
            if (fieldEntityIdChangeListeners.size() > 0 || scopeEntityIdChangeListeners.size() > 0) {
                JSONArray listenersArray = new JSONArray();
                for (ComponentPattern listener : fieldEntityIdChangeListeners.values()) {
                    listenersArray.put(listener.getPathName());
                }
                for (ComponentPattern listener : scopeEntityIdChangeListeners.values()) {
                    listenersArray.put(listener.getPathName());
                }
                jsOptions.put("listeners", listenersArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsOptions;
    }
}
