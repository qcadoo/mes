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
package com.qcadoo.mes.materialFlowResources.rowStyleResolvers;

import com.google.common.collect.Sets;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.constants.RowStyle;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WarehouseStocksListResolver {

    public Set<String> fillRowStyles(final Entity warehouseStocks) {
        final Set<String> rowStyles = Sets.newHashSet();

        if (warehouseStocks.getDecimalField("minimumState") != null) {

            if (BigDecimalUtils.convertNullToZero(warehouseStocks.getDecimalField("minimumState"))
                    .compareTo(BigDecimalUtils.convertNullToZero(warehouseStocks.getDecimalField("quantity"))) == 1) {
                rowStyles.add(RowStyle.RED_BACKGROUND);
            }

        }

        return rowStyles;
    }

}
