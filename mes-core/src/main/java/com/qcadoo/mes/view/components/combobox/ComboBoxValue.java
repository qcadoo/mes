package com.qcadoo.mes.view.components.combobox;

import java.util.List;

public final class ComboBoxValue {

    private String selectedValue;

    private List<String> values;

    public ComboBoxValue(final List<String> values, final String selectedValue) {
        super();
        this.values = values;
        this.selectedValue = selectedValue;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(final String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(final List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return selectedValue;
    }

}
