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

package com.qcadoo.mes.view.components;

/**
 * View value of SimpleFieldComponent.
 * 
 * @see com.qcadoo.mes.view.components.SimpleFieldComponent
 * @see com.qcadoo.mes.view.ViewValue
 */
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

    public final Object getValue() {
        return value;
    }

    public final void setValue(final Object value) {
        this.value = value;
    }

    @Override
    public final String toString() {
        return String.valueOf(value);
    }

}
