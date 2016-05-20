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
package com.qcadoo.mes.materialFlowResources.constants;

public enum WarehouseAlgorithm {
    FIFO("01fifo"), LIFO("02lifo"), FEFO("03fefo"), LEFO("04lefo"), MANUAL("05manual");

    private final String value;

    private WarehouseAlgorithm(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static WarehouseAlgorithm parseString(final String type) {
        if (LIFO.getStringValue().equalsIgnoreCase(type)) {
            return LIFO;
        } else if (FEFO.getStringValue().equalsIgnoreCase(type)) {
            return FEFO;
        } else if (LEFO.getStringValue().equalsIgnoreCase(type)) {
            return LEFO;
        } else if (MANUAL.getStringValue().equalsIgnoreCase(type)) {
            return MANUAL;
        } else {
            return FIFO;
        }
    }

}
