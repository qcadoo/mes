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
package com.qcadoo.mes.techSubcontrForOrderSupplies.constants;

public enum ProductExtractedTSFOS {
    FROM_DELIVERERS("02fromDeliverers"), FROM_SUBCONCTRACTORS("03fromSubcontractors");

    private final String productExtracted;

    private ProductExtractedTSFOS(final String productExtracted) {
        this.productExtracted = productExtracted;
    }

    public String getStringValue() {
        return productExtracted;
    }

    public static ProductExtractedTSFOS parseString(final String string) {
        if ("02fromDeliverers".equals(string)) {
            return FROM_DELIVERERS;
        } else if ("03fromSubcontractors".equals(string)) {
            return FROM_DELIVERERS;
        }

        throw new IllegalStateException("Unsupported productExtracted: " + string);
    }

}
