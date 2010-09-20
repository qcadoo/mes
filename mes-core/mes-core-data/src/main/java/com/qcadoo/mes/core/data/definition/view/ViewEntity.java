package com.qcadoo.mes.core.data.definition.view;

import java.util.HashMap;
import java.util.Map;

public class ViewEntity<T> {

    private boolean hidden;

    private boolean enabled;

    private T value;

    private final Map<String, ViewEntity<?>> components = new HashMap<String, ViewEntity<?>>();

    public ViewEntity() {
    }

    public ViewEntity(final T value) {
        this.value = value;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public Map<String, ViewEntity<?>> getComponents() {
        return components;
    }

    public ViewEntity<?> getComponent(final String name) {
        return components.get(name);
    }

    public void addComponent(final String name, final ViewEntity<?> value) {
        this.components.put(name, value);
    }

}
