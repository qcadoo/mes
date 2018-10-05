/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productFlowThruDivision.constants;

import org.apache.commons.lang3.StringUtils;


public enum WarehouseIssueProductsSource {

    ORDER("01order"), MANUAL("02manual");

    private final String strValue;

    private WarehouseIssueProductsSource(final String strValue) {
        this.strValue = strValue;
    }

    public String getStrValue() {
        return strValue;
    }

    public static WarehouseIssueProductsSource parseString(final String stringValue) {
        for (WarehouseIssueProductsSource warehouseIssueProductsSource : values()) {
            if (StringUtils.equalsIgnoreCase(stringValue, warehouseIssueProductsSource.getStrValue())) {
                return warehouseIssueProductsSource;
            }
        }
        throw new IllegalArgumentException(String.format("Can't parse WarehouseIssueProductsSource enum instance from '%s'",
                stringValue));
    }
}
