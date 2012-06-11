/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.orders.constants;

import com.google.common.base.Preconditions;

public enum OrderState {

    PENDING("01pending"), ACCEPTED("02accepted"), IN_PROGRESS("03inProgress"), COMPLETED("04completed"), DECLINED("05declined"), INTERRUPTED(
            "06interrupted"), ABANDONED("07abandoned");

    private final String stringValue;

    private OrderState(final String state) {
        this.stringValue = state;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static OrderState parseString(final String string) {
        OrderState parsedStatus = null;
        for (OrderState status : OrderState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse OrderState from string '" + string + "'");
        return parsedStatus;
    }

}
