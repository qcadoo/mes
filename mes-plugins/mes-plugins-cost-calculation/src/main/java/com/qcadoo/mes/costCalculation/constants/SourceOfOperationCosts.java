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
package com.qcadoo.mes.costCalculation.constants;

import org.apache.commons.lang3.StringUtils;

public enum SourceOfOperationCosts {
    TECHNOLOGY_OPERATION("01technologyOperation"), PARAMETERS("02parameters");

    private final String sourceOfOperationCosts;

    private SourceOfOperationCosts(final String sourceOfOperationCosts) {
        this.sourceOfOperationCosts = sourceOfOperationCosts;
    }

    public String getStringValue() {
        return sourceOfOperationCosts;
    }

    public static SourceOfOperationCosts parseString(final String rawStringValue) {
        for (SourceOfOperationCosts sourceOfOperationCosts : values()) {
            if (StringUtils.equalsIgnoreCase(rawStringValue, sourceOfOperationCosts.getStringValue())) {
                return sourceOfOperationCosts;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot parse SourceOfOperationCosts from '%s'", rawStringValue));
    }

}
