/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view.components.combobox;

import java.util.List;

import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.view.components.SimpleValue;

/**
 * View value of DynamicComboBoxComponent.
 * 
 * @see com.qcadoo.mes.view.components.DynamicComboBoxComponent
 * @see com.qcadoo.mes.view.ViewValue
 */
public final class ComboBoxValue extends SimpleValue {

    private List<Pair<String, String>> values;

    public ComboBoxValue(final List<Pair<String, String>> values, final String selectedValue) {
        super(selectedValue);
        this.values = values;
    }

    public String getSelectedValue() {
        return (String) getValue();
    }

    public void setSelectedValue(final String selectedValue) {
        setValue(selectedValue);
    }

    public List<Pair<String, String>> getValues() {
        return values;
    }

    public void setValues(final List<Pair<String, String>> values) {
        this.values = values;
    }

}
