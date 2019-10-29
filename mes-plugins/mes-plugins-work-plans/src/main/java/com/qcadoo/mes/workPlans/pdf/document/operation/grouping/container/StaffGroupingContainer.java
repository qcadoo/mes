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

import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

public class StaffGroupingContainer extends AbstractGroupingContainer {

    private String nullStaffTitle;

    public StaffGroupingContainer(final Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment,
            final String titleAppend, final String nullWonullStaffTitlerkstationTitle) {
        super(orderColumnToAlignment, operationComponentIdProductInColumnToAlignment,
                operationComponentIdProductOutColumnToAlignment, titleAppend);
        this.nullStaffTitle = nullStaffTitle;
    }

    @Override
    public void add(final Entity order, final Entity operationComponent,
            final OperationProductComponentWithQuantityContainer productQuantities) {
        Entity operationalTask = extractOperationalTask(order, operationComponent);

        if (Objects.isNull(operationalTask)) {
            store(nullStaffTitle, order, operationComponent);
        } else {
            Entity staff = operationalTask.getBelongsToField(OperationalTaskFields.STAFF);

            if (Objects.isNull(staff)) {
                store(nullStaffTitle, order, operationComponent);
            } else {
                store(title(staff), order, operationComponent);
            }
        }
    }

    private String title(final Entity staff) {
        String staffName = staff.getStringField(StaffFields.NAME);
        String staffSurname = staff.getStringField(StaffFields.SURNAME);

        return new StringBuilder(titleAppend).append(" ").append(staffName).append(" ").append(staffSurname).toString();
    }

    private Entity extractOperationalTask(final Entity order, final Entity operationComponent) {
        return order.getHasManyField(OrderFields.OPERATIONAL_TASKS).find()
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, operationComponent))
                .add(SearchRestrictions.ne(OperationalTaskFields.STATE, OperationalTaskStateStringValues.REJECTED))
                .setMaxResults(1).uniqueResult();
    }

}
