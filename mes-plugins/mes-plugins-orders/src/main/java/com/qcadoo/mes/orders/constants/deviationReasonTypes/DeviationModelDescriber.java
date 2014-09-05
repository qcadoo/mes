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
package com.qcadoo.mes.orders.constants.deviationReasonTypes;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DeviationModelDescriber {

    private final String modelPlugin;

    private final String modelName;

    private final String reasonTypeFieldName;

    public DeviationModelDescriber(final String modelPlugin, final String modelName, final String reasonTypeFieldName) {
        this.modelPlugin = modelPlugin;
        this.modelName = modelName;
        this.reasonTypeFieldName = reasonTypeFieldName;
    }

    public String getModelPlugin() {
        return modelPlugin;
    }

    public String getModelName() {
        return modelName;
    }

    public String getReasonTypeFieldName() {
        return reasonTypeFieldName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DeviationModelDescriber rhs = (DeviationModelDescriber) obj;
        return new EqualsBuilder().append(this.modelPlugin, rhs.modelPlugin).append(this.modelName, rhs.modelName)
                .append(this.reasonTypeFieldName, rhs.reasonTypeFieldName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelPlugin).append(modelName).append(reasonTypeFieldName).toHashCode();
    }
}
