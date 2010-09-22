package com.qcadoo.mes.core.data.view.elements.comboBox;

import java.util.List;

public class ComboBoxValue {

    private String selectedValue;

    private List<String> values;

    public ComboBoxValue(List<String> values, String selectedValue) {
        super();
        this.values = values;
        this.selectedValue = selectedValue;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return selectedValue;
    }

}
