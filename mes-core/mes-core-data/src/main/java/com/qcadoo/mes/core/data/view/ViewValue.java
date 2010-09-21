package com.qcadoo.mes.core.data.view;

import java.util.HashMap;
import java.util.Map;

public class ViewValue<T> {

    private boolean visible = true;

    private boolean enabled = true;

    private T value;

    private final Map<String, ViewValue<?>> components = new HashMap<String, ViewValue<?>>();

    public ViewValue() {
    }

    public ViewValue(final T value) {
        this.value = value;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
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

    public Map<String, ViewValue<?>> getComponents() {
        return components;
    }

    public ViewValue<?> getComponent(final String name) {
        return components.get(name);
    }

    public void addComponent(final String name, final ViewValue<?> value) {
        this.components.put(name, value);
    }

}
