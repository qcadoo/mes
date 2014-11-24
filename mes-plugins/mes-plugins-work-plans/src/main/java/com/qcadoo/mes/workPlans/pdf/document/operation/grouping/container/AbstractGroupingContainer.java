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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;

import java.util.*;

public abstract class AbstractGroupingContainer implements GroupingContainer {

    protected String titleAppend;

    private Map<OrderColumn, ColumnAlignment> orderColumnToAlignment;
    private Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment;
    private Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment;
    private ListMultimap<String, OrderOperationComponent> map = LinkedListMultimap.create();
    private Set<Entity> orders = new HashSet<Entity>();

    protected AbstractGroupingContainer(Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
                                        Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment,
                                        Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment,
                                        String titleAppend) {

        this.orderColumnToAlignment = orderColumnToAlignment;
        this.operationComponentIdProductInColumnToAlignment = operationComponentIdProductInColumnToAlignment;
        this.operationComponentIdProductOutColumnToAlignment = operationComponentIdProductOutColumnToAlignment;
        this.titleAppend = titleAppend;
    }

    protected void store(String title, Entity order, Entity operationComponent) {
        map.put(title, new OrderOperationComponent(order, operationComponent));
        orders.add(order);
    }

    @Override
    public ListMultimap<String, OrderOperationComponent> getTitleToOperationComponent() {
        return map;
    }

    @Override
    public List<Entity> getOrders() {
        return new ArrayList<Entity>(orders);
    }

    @Override
    public boolean hasManyOrders() {
        return orders.size() > 1;
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
