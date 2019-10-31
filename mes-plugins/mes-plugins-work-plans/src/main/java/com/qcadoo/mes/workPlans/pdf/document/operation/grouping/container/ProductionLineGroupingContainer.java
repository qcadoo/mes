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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container;

import java.util.Map;
import java.util.Objects;

import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;

public class ProductionLineGroupingContainer extends AbstractGroupingContainer {

    protected String nullProductionLineTitle;

    public ProductionLineGroupingContainer(final Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment,
            final String titleAppend, final String nullProductionLineTitle) {
        super(orderColumnToAlignment, operationComponentIdProductInColumnToAlignment,
                operationComponentIdProductOutColumnToAlignment, titleAppend);

        this.nullProductionLineTitle = nullProductionLineTitle;
    }

    @Override
    public void add(final Entity order, final Entity operationComponent,
            final OperationProductComponentWithQuantityContainer productQuantities) {
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        if (Objects.isNull(productionLine)) {
            store(nullProductionLineTitle, order, operationComponent);
        } else {
            store(title(productionLine), order, operationComponent);
        }
    }

    private String title(final Entity productionLine) {
        String productionLineName = productionLine.getStringField(ProductionLineFields.NAME);

        return new StringBuilder(titleAppend).append(" ").append(productionLineName).toString();
    }

}
