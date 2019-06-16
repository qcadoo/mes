/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

public enum OperationalTaskType {
    OTHER_CASE("01otherCase"),EXECUTION_OPERATION_IN_ORDER("02executionOperationInOrder");

    private final String operationalTaskType;

    OperationalTaskType(final String operationalTaskType) {
        this.operationalTaskType = operationalTaskType;
    }

    public String getStringValue() {
        return operationalTaskType;
    }

    public static OperationalTaskType parseString(final String string) {
        for (OperationalTaskType type : values()) {
            if (type.getStringValue().equals(string)) {
                return type;
            }
        }

        throw new IllegalStateException("Unsupported OperationalTaskType: " + string);
    }

}
