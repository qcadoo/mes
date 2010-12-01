/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold.components.combobox;

import java.util.Map;

import com.qcadoo.mes.viewold.components.SimpleValue;

/**
 * View value of EntityComboBoxComponent.
 * 
 * @see com.qcadoo.mes.viewold.components.EntityComboBoxComponent
 * @see com.qcadoo.mes.viewold.ViewValue
 */

@Deprecated
public final class EntityComboBoxValue extends SimpleValue {

    private Map<Long, String> values;

    public Long getSelectedValue() {
        return (Long) getValue();
    }

    public void setSelectedValue(final Long selectedValue) {
        setValue(selectedValue);
    }

    public Map<Long, String> getValues() {
        return values;
    }

    public void setValues(final Map<Long, String> values) {
        this.values = values;
    }

}
