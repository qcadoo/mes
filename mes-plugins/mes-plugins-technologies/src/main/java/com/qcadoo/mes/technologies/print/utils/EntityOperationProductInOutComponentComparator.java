/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.technologies.print.utils;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.model.api.Entity;

public class EntityOperationProductInOutComponentComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = 1371136330724619528L;

    private static final String PRODUCT_FIELD = "product";

    @Override
    public final int compare(final Entity o1, final Entity o2) {
        int result = o1.getBelongsToField("operationComponent").getBelongsToField("operation").getId().toString()
                .compareTo(o2.getBelongsToField("operationComponent").getBelongsToField("operation").getId().toString());
        if (result != 0) {
            return result;
        }

        result = o1.getDataDefinition().getName().compareTo(o2.getDataDefinition().getName());
        if (result != 0) {
            return result;
        }

        result = o1.getBelongsToField(PRODUCT_FIELD).getStringField("name")
                .compareTo(o2.getBelongsToField(PRODUCT_FIELD).getStringField("name"));
        if (result != 0) {
            return result;
        }

        result = o1.getField("quantity").toString().compareTo(o2.getField("quantity").toString());
        if (result != 0) {
            return result;
        }

        return o1.getBelongsToField(PRODUCT_FIELD).getStringField("unit")
                .compareTo(o2.getBelongsToField(PRODUCT_FIELD).getStringField("unit"));
    }
}
