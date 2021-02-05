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

import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StaffGroupingContainer extends AbstractGroupingContainer {

    private String nullStaffTitle;

    public StaffGroupingContainer(final Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment,
            final String titleAppend, final String nullStaffTitle) {
        super(orderColumnToAlignment, operationComponentIdProductInColumnToAlignment,
                operationComponentIdProductOutColumnToAlignment, titleAppend);
        this.nullStaffTitle = nullStaffTitle;
    }


    @Override
    public void add(Entity order, Entity operationComponent, List<Entity> productionCountingQuantitiesIn,
            List<Entity> productionCountingQuantitiesOut) {

    }

    @Override
    public void add(final OrderOperationComponent orderOperationComponent) {

        Entity operationalTask = extractOperationalTask(orderOperationComponent.getOrder(), orderOperationComponent.getOperationComponent());

        if (Objects.isNull(operationalTask)) {
            store(nullStaffTitle, orderOperationComponent);
        } else {
            Entity staff = operationalTask.getBelongsToField(OperationalTaskFields.STAFF);

            if (Objects.isNull(staff)) {
                store(nullStaffTitle, orderOperationComponent);
            } else {
                store(title(staff), orderOperationComponent);
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
