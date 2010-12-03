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

package com.qcadoo.mes.view.components.combobox;

import java.util.Map;

import com.qcadoo.mes.view.components.SimpleValue;

/**
 * View value of EntityComboBoxComponent.
 * 
 * @see com.qcadoo.mes.view.components.EntityComboBoxComponent
 * @see com.qcadoo.mes.view.ViewValue
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
