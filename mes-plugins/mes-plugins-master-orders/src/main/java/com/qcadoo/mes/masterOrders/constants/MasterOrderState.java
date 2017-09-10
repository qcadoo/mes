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
package com.qcadoo.mes.masterOrders.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;

public enum MasterOrderState {

    NEW("01new"), IN_EXECUTION("02inExecution"), COMPLETED("03completed"), DECLINED("04declined");

    private final String state;

    private MasterOrderState(final String state) {
        this.state = state;
    }

    public String getStringValue() {
        return state;
    }

    public static MasterOrderState of(final Entity entity) {
        Preconditions.checkArgument(entity != null, "Missing entity");
        return parseString(entity.getStringField(MasterOrderFields.STATE));
    }

    public static MasterOrderState parseString(final String state) {
        String masterOrderState = StringUtils.trim(state);
        for (MasterOrderState type : values()) {
            if (StringUtils.equalsIgnoreCase(type.getStringValue(), masterOrderState)) {
                return type;
            }
        }

        throw new IllegalStateException("Couldn't parse MasterOrderState from string '" + state + "'");
    }

}
