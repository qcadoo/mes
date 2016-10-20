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
package com.qcadoo.mes.productionPerShift.report.print.utils;

import java.io.Serializable;
import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.model.api.Entity;

public class EntityProductionPerShiftsComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = 4166952272166395581L;

    @Override
    public int compare(final Entity arg0, final Entity arg1) {
        return ComparisonChain.start().compare( arg0
                .getBelongsToField(ProductionPerShiftFields.ORDER)
                .getBelongsToField(OrderFields.PRODUCTION_LINE)
                .getStringField(ProductionLineFields.NUMBER),  arg1
                .getBelongsToField(ProductionPerShiftFields.ORDER)
                .getBelongsToField(OrderFields.PRODUCTION_LINE)
                .getStringField(ProductionLineFields.NUMBER)).compare(arg0
                .getBelongsToField(ProductionPerShiftFields.ORDER).getDateField(OrderFields.START_DATE), arg1
                .getBelongsToField(ProductionPerShiftFields.ORDER).getDateField(OrderFields.START_DATE)).result();
    }

}
