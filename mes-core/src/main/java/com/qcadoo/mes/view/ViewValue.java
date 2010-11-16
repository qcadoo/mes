/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Objects holds value of the component. This value is serializable to json and used to client/server communication.
 * 
 * @param <T>
 *            class of the value
 */
public final class ViewValue<T> {

    private Boolean visible;

    private Boolean enabled;

    private String updateMode;

    private final List<String> errorMessages = new ArrayList<String>();

    private final List<String> infoMessages = new ArrayList<String>();

    private final List<String> successMessages = new ArrayList<String>();

    private T value;

    private final Map<String, ViewValue<?>> components = new HashMap<String, ViewValue<?>>();

    /**
     * Create new empty value.
     */
    public ViewValue() {
    }

    /**
     * Create new value.
     * 
     * @param value
     *            value
     */
    public ViewValue(final T value) {
        this.value = value;
    }

    /**
     * Return true if component should be visible.
     * 
     * @return is visible
     */
    public Boolean isVisible() {
        return visible;
    }

    /**
     * Set visible flag of the component.
     * 
     * @param visible
     *            visible
     */
    public void setVisible(final Boolean visible) {
        this.visible = visible;
    }

    /**
     * Return true if component should be enable (not readonly).
     * 
     * @return is enabled
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Set enable flag of the component.
     * 
     * @param enabled
     *            enabled
     */
    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Return component value.
     * 
     * @return value
     */
    public T getValue() {
        return value;
    }

    /**
     * Set component value.
     * 
     * @param value
     *            value
     */
    public void setValue(final T value) {
        this.value = value;
    }

    /**
     * Return update mode of the component - "update" or "ignore" - if "ignore" value of this component will not be returned to
     * the client.
     * 
     * @return update mode
     */
    public String getUpdateMode() {
        return updateMode;
    }

    /**
     * Return true if update mode is "ignore" - value of this component will not be returned to the client.
     * 
     * @return ignore mode
     */
    public boolean isIgnoreMode() {
        return "ignore".equals(updateMode);
    }

    /**
     * Set update mode of the component.
     * 
     * @param updateMode
     *            update mode
     */
    public void setUpdateMode(final String updateMode) {
        this.updateMode = updateMode;
    }

    /**
     * Return values of the child components.
     * 
     * @return child values
     */
    public Map<String, ViewValue<?>> getComponents() {
        return components;
    }

    /**
     * Return value of the children component.
     * 
     * @param name
     *            children component's name
     * @return children value
     */
    public ViewValue<?> getComponent(final String name) {
        return components.get(name);
    }

    /**
     * Add value of the children component.
     * 
     * @param name
     *            children component's name
     * @param value
     *            children component's value
     */
    public void addComponent(final String name, final ViewValue<?> value) {
        this.components.put(name, value);
    }

    /**
     * Return all messages related with this component.
     * 
     * @return messages
     */
    public List<Map<String, String>> getMessages() {
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();

        for (String message : errorMessages) {
            messages.add(ImmutableMap.of("message", message, "type", "error"));
        }

        for (String message : successMessages) {
            messages.add(ImmutableMap.of("message", message, "type", "success"));
        }

        for (String message : infoMessages) {
            messages.add(ImmutableMap.of("message", message, "type", "info"));
        }

        return messages;
    }

    /**
     * Return error messages related with this component.
     * 
     * @return messages
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    /**
     * Add error message to this component.
     * 
     * @param errorMessage
     *            message
     */
    public void addErrorMessage(final String errorMessage) {
        this.errorMessages.add(errorMessage);
    }

    /**
     * Return info messages related with this component.
     * 
     * @return messages
     */
    public List<String> getInfoMessages() {
        return infoMessages;
    }

    /**
     * Add info message to this component.
     * 
     * @param infoMessage
     *            message
     */
    public void addInfoMessage(final String infoMessage) {
        this.infoMessages.add(infoMessage);
    }

    /**
     * Return success messages related with this component.
     * 
     * @return messages
     */
    public List<String> getSuccessMessages() {
        return successMessages;
    }

    /**
     * Add success message to this component.
     * 
     * @param successMessage
     *            message
     */
    public void addSuccessMessage(final String successMessage) {
        this.successMessages.add(successMessage);
    }

    /**
     * Find value of the component with given path.
     * 
     * @param path
     *            path
     * @return value
     */
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