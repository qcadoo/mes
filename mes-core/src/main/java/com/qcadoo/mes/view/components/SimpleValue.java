package com.qcadoo.mes.view.components;

public class SimpleValue {

    private Object value;

    private boolean required;

    public SimpleValue() {
    }

    public SimpleValue(final Object value) {
        this.value = value;
    }

    public final boolean isRequired() {
        return required;
    }

    public final void setRequired(final boolean required) {
        this.required = required;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    @Override
    public final String toString() {
        return String.valueOf(value);
    }

}
