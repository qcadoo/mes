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

import java.util.Map;

import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;

public class WorkstationTypeGroupingContainer extends AbstractGroupingContainer {

    protected String nullWorkstationTitle;

    public WorkstationTypeGroupingContainer(Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
            Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment,
            Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment,
            String titleAppend,
            String nullWorkstationTitle) {
        super(orderColumnToAlignment, operationComponentIdProductInColumnToAlignment,
                operationComponentIdProductOutColumnToAlignment, titleAppend);
        this.nullWorkstationTitle = nullWorkstationTitle;
    }

    @Override
    public void add(Entity order, Entity operationComponent, OperationProductComponentWithQuantityContainer productQuantities) {
        Entity workstationType = workstationType(operationComponent);
        if (workstationType == null) {
            store(nullWorkstationTitle, order, operationComponent);
        } else {
            store(title(workstationType), order, operationComponent);
        }
    }

    private String title(Entity workstationType) {
        String workstationName = workstationType.getStringField(WorkstationTypeFields.NAME);
        return titleAppend + " " + workstationName;
    }

    private Entity workstationType(Entity operationComponent) {
        return operationComponent.getBelongsToField(TechnologyOperationComponentFields.WORKSTATION_TYPE);
    }

}
