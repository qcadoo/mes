package com.qcadoo.mes.core.data.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewValue<T> {

    private boolean visible = true;

    private boolean enabled = true;

    private final List<String> errorMessages = new ArrayList<String>();

    private final List<String> infoMessages = new ArrayList<String>();

    private final List<String> successMessages = new ArrayList<String>();

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

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void addErrorMessage(final String errorMessage) {
        this.errorMessages.add(errorMessage);
    }

    public List<String> getInfoMessages() {
        return infoMessages;
    }

    public void addInfoMessage(final String infoMessage) {
        this.infoMessages.add(infoMessage);
    }

    public List<String> getSuccessMessages() {
        return successMessages;
    }

    public void addSuccessMessage(final String successMessage) {
        this.successMessages.add(successMessage);
    }

    public ViewValue<?> lookupValue(final String path) {
        String[] fields = path.split("\\.");

        ViewValue<?> viewValue = this;

        for (String field : fields) {
            viewValue = viewValue.getComponent(field);
            if (viewValue == null) {
                return null;
            }
        }

        return viewValue;
    }

}
