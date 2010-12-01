package com.qcadoo.mes.newview;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.util.StringUtils.hasText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.viewold.ComponentOption;

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

    private boolean defaultEnabled;

    private boolean defaultVisible;

    private boolean hasDescription;

    private String reference;

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
    public void initialize(final ViewDefinition viewDefinition) {
        if (dataDefinition != null) {
            return; // already initialized
        }

        String[] field = null;
        String[] scopeField = null;
        AbstractComponentPattern fieldComponent = null;
        AbstractComponentPattern scopeFieldComponent = null;

        if (fieldPath != null) {
            field = getComponentAndField(fieldPath);
            fieldComponent = (AbstractComponentPattern) (field[0] == null ? parent : viewDefinition.getComponentByPath(field[0]));
            fieldComponent.addFieldEntityIdChangeListener(field[1], this);
        }

        if (scopeFieldPath != null) {
            scopeField = getComponentAndField(scopeFieldPath);
            scopeFieldComponent = (AbstractComponentPattern) (scopeField[0] == null ? parent : viewDefinition
                    .getComponentByPath(scopeField[0]));
            scopeFieldComponent.addScopeEntityIdChangeListener(scopeField[1], this);
        }

        getDataDefinition(viewDefinition, fieldComponent, scopeFieldComponent);

        getFieldAndScopeFieldDefinitions(field, scopeField);

        getDataDefinitionFromFieldDefinition();

        reinitializeListeners(viewDefinition);
    }

    private void getDataDefinitionFromFieldDefinition() {
        if (fieldDefinition != null) {
            getDataDefinitionFromFieldDefinition(fieldDefinition);
        } else if (scopeFieldDefinition != null) {
            getDataDefinitionFromFieldDefinition(scopeFieldDefinition);
        }
    }

    private void getFieldAndScopeFieldDefinitions(String[] field, String[] scopeField) {
        if (dataDefinition != null) {
            if (fieldPath != null) {
                fieldDefinition = dataDefinition.getField(field[1]);
            }

            if (scopeFieldPath != null) {
                scopeFieldDefinition = dataDefinition.getField(scopeField[1]);
            }
        }
    }

    private void getDataDefinition(final ViewDefinition viewDefinition, AbstractComponentPattern fieldComponent,
            AbstractComponentPattern scopeFieldComponent) {
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

    private void reinitializeListeners(final ViewDefinition viewDefinition) {
        for (ComponentPattern componentPattern : fieldEntityIdChangeListeners.values()) {
            ((AbstractComponentPattern) componentPattern).initialize(viewDefinition);
        }

        for (ComponentPattern componentPattern : scopeEntityIdChangeListeners.values()) {
            ((AbstractComponentPattern) componentPattern).initialize(viewDefinition);
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
        ComponentState componentState = getComponentStateInstance();
        // TODO mina

        return componentState;
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
        // TODO Auto-generated method stub

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

}
