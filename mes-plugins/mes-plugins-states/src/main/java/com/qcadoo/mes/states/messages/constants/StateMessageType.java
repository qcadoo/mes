/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.states.messages.constants;

import com.google.common.base.Preconditions;

public enum StateMessageType {

    SUCCESS("01success"), INFO("02info"), FAILURE("03failure"), VALIDATION_ERROR("04validationError");

    private final String stringValue;

    private StateMessageType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public static StateMessageType parseString(final String string) {
        StateMessageType parsedValue = null;
        for (StateMessageType status : StateMessageType.values()) {
            if (status.getStringValue().equals(string)) {
                parsedValue = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedValue != null, "Couldn't parse string '" + string + "'");
        return parsedValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
