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
package com.qcadoo.mes.productionCounting.constants;

public enum ProductionBalanceType {
    ONE_ORDER("01oneOrder"), MANY_ORDERS("02manyOrders");

    private final String type;

    private ProductionBalanceType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static ProductionBalanceType parseString(final String string) {
        if ("01oneOrder".equals(string)) {
            return ONE_ORDER;
        } else if ("02manyOrders".equals(string)) {
            return MANY_ORDERS;
        }

        throw new IllegalStateException("Unsupported ProductionBalanceType: " + string);
    }

}
