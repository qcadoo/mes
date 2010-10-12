package com.qcadoo.mes.view.components.combobox;

import java.util.List;

import com.qcadoo.mes.view.components.SimpleValue;

public final class ComboBoxValue extends SimpleValue {

    private List<String> values;

    public ComboBoxValue(final List<String> values, final String selectedValue) {
        super(selectedValue);
        this.values = values;
    }

    public String getSelectedValue() {
        return (String)getValue();
    }

    public void setSelectedValue(final String selectedValue) {
        setValue(selectedValue);
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(final List<String> values) {
        this.values = values;
    }

}
