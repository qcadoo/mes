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
package com.qcadoo.mes.masterOrders.constants;

import org.apache.commons.lang.StringUtils;

import com.qcadoo.model.api.Entity;

public enum MasterOrderType {

    UNDEFINED("01undefined"), ONE_PRODUCT("02oneProduct"), MANY_PRODUCTS("03manyProducts");

    private final String masterOrderType;

    private MasterOrderType(final String masterOrderType) {
        this.masterOrderType = masterOrderType;
    }

    public String getStringValue() {
        return masterOrderType;
    }

    public static MasterOrderType of(final Entity masterOrderEntity) {
        return parseString(masterOrderEntity.getStringField(MasterOrderFields.MASTER_ORDER_TYPE));
    }

    public static MasterOrderType parseString(final String rawMasterOrderType) {
        if (StringUtils.isBlank(rawMasterOrderType)) {
            return UNDEFINED;
        }

        String masterOrderType = StringUtils.trim(rawMasterOrderType);
        for (MasterOrderType type : values()) {
            if (StringUtils.equalsIgnoreCase(type.getStringValue(), masterOrderType)) {
                return type;
            }
        }

        throw new IllegalStateException("Unsupported masterOrderType: " + masterOrderType);
    }

}
