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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractGroupingContainer implements GroupingContainer {

    protected String titleAppend;

    private final Map<OrderColumn, ColumnAlignment> orderColumnToAlignment;

    private final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment;

    private final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment;

    private final ListMultimap<String, OrderOperationComponent> map = LinkedListMultimap.create();

    private final Set<Entity> orders = Sets.newHashSet();

    protected AbstractGroupingContainer(final Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment,
            final String titleAppend) {
        this.orderColumnToAlignment = orderColumnToAlignment;
        this.operationComponentIdProductInColumnToAlignment = operationComponentIdProductInColumnToAlignment;
        this.operationComponentIdProductOutColumnToAlignment = operationComponentIdProductOutColumnToAlignment;
        this.titleAppend = titleAppend;
    }

    protected void store(final String title, final OrderOperationComponent orderOperationComponent) {
        map.put(title, orderOperationComponent);
        orders.add(orderOperationComponent.getOrder());
    }

    @Override
    public ListMultimap<String, OrderOperationComponent> getTitleToOperationComponent() {
        return map;
    }

    @Override
    public List<Entity> getOrders() {
        return Lists.newArrayList(orders);
    }

    @Override
    public Map<OrderColumn, ColumnAlignment> getOrderColumnToAlignment() {
        return orderColumnToAlignment;
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationComponentIdProductInColumnToAlignment() {
        return operationComponentIdProductInColumnToAlignment;
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationComponentIdProductOutColumnToAlignment() {
        return operationComponentIdProductOutColumnToAlignment;
    }

}
