package com.qcadoo.mes.newview;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qcadoo.mes.view.ComponentOption;

public abstract class AbstractComponentPattern implements ComponentPattern {

    // private ViewDefinition viewDefinition;
    // private Component<?> sourceComponent;
    // private boolean initialized;
    // private final List<ComponentOption> rawOptions = new ArrayList<ComponentOption>();
    // private final Map<String, Object> options = new HashMap<String, Object>();
    // private final ContainerComponent<?> parentContainer;
    // private final TranslationService translationService;
    // private Set<String> listeners = new HashSet<String>();
    // private DataDefinition dataDefinition;
    // private Ribbon ribbon;
    // private boolean defaultEnabled = true;
    // private boolean defaultVisible = true;
    // private boolean hasDescription = false;

    private final String name;

    private final String fieldPath;

    private final String sourceFieldPath;

    private final ComponentPattern parent;

    private final Map<String, ComponentPattern> fieldEntityIdChangeListeners = new HashMap<String, ComponentPattern>();

    public AbstractComponentPattern(final String name, final String fieldPath, final String sourceFieldPath,
            final ComponentPattern parent) {
        this.name = name;
        this.fieldPath = fieldPath;
        this.sourceFieldPath = sourceFieldPath;
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
        if (fieldPath != null) {
            Pattern p = Pattern.compile("^#\\{.+\\}\\.");
            Matcher m = p.matcher(fieldPath);
            if (m.find()) {
                String field = fieldPath.substring(m.end());
                String componentPath = fieldPath.substring(2, m.end() - 2);
                ((AbstractComponentPattern) viewDefinition.getComponentByPath(componentPath)).addFieldEntityIdChangeListener(
                        field, this);
            } else {
                ((AbstractComponentPattern) parent).addFieldEntityIdChangeListener(fieldPath, this);
            }
        }
    }

    public void addFieldEntityIdChangeListener(final String field, final ComponentPattern listener) {
        fieldEntityIdChangeListeners.put(field, listener);
    }

    protected Map<String, ComponentPattern> getFieldEntityIdChangeListenersMap() {
        return fieldEntityIdChangeListeners;
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

    }

    public void setDefaultEnabled(final boolean booleanAttribute) {
        // TODO Auto-generated method stub

    }

    public void setDefaultVisible(final boolean booleanAttribute) {
        // TODO Auto-generated method stub

    }

    public void setHasDescription(final boolean booleanAttribute) {
        // TODO Auto-generated method stub

    }

    public void addOption(final ComponentOption option) {
        // TODO Auto-generated method stub

    }

    public void setReference(final String stringAttribute) {
        // TODO Auto-generated method stub

    }
}
