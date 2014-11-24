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
package com.qcadoo.mes.operationTimeCalculations.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.model.api.Entity;

public class OperationTimes {

    private final Entity operation;

    private final OperationWorkTime times;

    public OperationTimes(final Entity operation, final OperationWorkTime workTimes) {
        this.operation = operation;
        this.times = workTimes;
    }

    public Entity getOperation() {
        return operation;
    }

    public OperationWorkTime getTimes() {
        return times;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(operation).append(times).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof OperationTimes)) {
            return false;
        }
        OperationTimes other = (OperationTimes) obj;
        return new EqualsBuilder().append(operation, other.operation).append(times, other.times).isEquals();
    }

}
