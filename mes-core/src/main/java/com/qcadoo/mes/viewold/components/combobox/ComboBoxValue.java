/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.viewold.components.combobox;

import java.util.List;

import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.viewold.components.SimpleValue;

/**
 * View value of DynamicComboBoxComponent.
 * 
 * @see com.qcadoo.mes.viewold.components.DynamicComboBoxComponent
 * @see com.qcadoo.mes.viewold.ViewValue
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
