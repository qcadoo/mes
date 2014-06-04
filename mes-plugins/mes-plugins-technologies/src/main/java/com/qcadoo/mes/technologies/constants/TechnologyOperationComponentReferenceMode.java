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
package com.qcadoo.mes.technologies.constants;

import com.google.common.base.Preconditions;

public enum TechnologyOperationComponentReferenceMode {

    REFERENCE("01reference"), COPY("02copy");

    private final String technologyOperationComponentReferenceMode;

    private TechnologyOperationComponentReferenceMode(final String technologyOperationComponentReferenceMode) {
        this.technologyOperationComponentReferenceMode = technologyOperationComponentReferenceMode;
    }

    public String getStringValue() {
        return this.technologyOperationComponentReferenceMode;
    }

    public static TechnologyOperationComponentReferenceMode parseString(final String string) {
        TechnologyOperationComponentReferenceMode referenceMode = null;
        for (TechnologyOperationComponentReferenceMode value : TechnologyOperationComponentReferenceMode.values()) {
            if (value.getStringValue().equals(string)) {
                referenceMode = value;
                break;
            }
        }
        Preconditions.checkArgument(referenceMode != null,
                "Couldn't parse TechnologyOperationComponentReferenceMode from string '" + string + "'");
        return referenceMode;
    }

}
