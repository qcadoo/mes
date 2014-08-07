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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;

import java.util.Map;

public class EndProductGroupingContainer extends AbstractGroupingContainer {

    public EndProductGroupingContainer(Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
                                       Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment,
                                       Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment,
                                       String titleAppend) {
        super(orderColumnToAlignment, operationComponentIdProductInColumnToAlignment, operationComponentIdProductOutColumnToAlignment, titleAppend);
    }

    @Override
    public void add(Entity order, Entity operationComponent) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if(technology == null)
            return;
        store(title(technology), order, operationComponent);
    }

    private String title(Entity technology) {
        Entity endProduct = technology.getBelongsToField(TechnologyFields.PRODUCT);
        String endProductName = endProduct.getStringField(ProductFields.NAME);
        return titleAppend + " " + endProductName;
    }

}
