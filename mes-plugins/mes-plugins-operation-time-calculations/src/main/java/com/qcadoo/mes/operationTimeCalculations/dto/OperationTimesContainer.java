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

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.model.api.Entity;

public class OperationTimesContainer {

    private final Map<Long, OperationTimes> timesByOperationId;

    public OperationTimesContainer() {
        this.timesByOperationId = Maps.newHashMap();
    }

    // TODO KASI what we should do when given operation already exists - override them or sum up?
    public void add(final Entity operation, final OperationWorkTime times) {
        Preconditions.checkNotNull(operation, "Missing operation entity!");
        Preconditions.checkNotNull(operation.getId(), "Operation has to have an id (be already persisted)!");
        OperationTimes operationTimes = new OperationTimes(operation, times);
        timesByOperationId.put(operation.getId(), operationTimes);
    }

    public OperationTimes get(final Long operationId) {
        Preconditions.checkNotNull(operationId, "Operation id have to be non-null Long value.");
        return timesByOperationId.get(operationId);
    }

    public Map<Long, OperationTimes> asMap() {
        return Collections.unmodifiableMap(timesByOperationId);
    }

    @Override
    public int hashCode() {
        return timesByOperationId.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OperationTimesContainer)) {
            return false;
        }

        OperationTimesContainer other = (OperationTimesContainer) obj;

        return timesByOperationId.equals(other.timesByOperationId);
    }

}
