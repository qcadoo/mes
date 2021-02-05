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

import com.google.common.collect.ListMultimap;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Map;

public class OperationProductInGroupingContainerDecorator implements GroupingContainer {

    private ProductionCountingService productionCountingService;

    private GroupingContainer groupingContainer;

    private ParameterService parameterService;

    private DataDefinitionService dataDefinitionService;

    public OperationProductInGroupingContainerDecorator(final DataDefinitionService dataDefinitionService,
            final GroupingContainer groupingContainer, final ProductionCountingService productionCountingService,
            final ParameterService parameterService) {
        this.groupingContainer = groupingContainer;
        this.productionCountingService = productionCountingService;
        this.parameterService = parameterService;
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override
    public void add(final Entity order, final Entity operationComponent, List<Entity> productionCountingQuantitiesIn,
            List<Entity> productionCountingQuantitiesOut) {

        OrderOperationComponent orderOperationComponent = new OrderOperationComponent(order, operationComponent,
                productionCountingQuantitiesIn, productionCountingQuantitiesOut);

        groupingContainer.add(orderOperationComponent);

    }

    @Override
    public void add(OrderOperationComponent orderOperationComponent) {

    }

    @Override
    public ListMultimap<String, OrderOperationComponent> getTitleToOperationComponent() {
        return groupingContainer.getTitleToOperationComponent();
    }

    @Override
    public List<Entity> getOrders() {
        return groupingContainer.getOrders();
    }

    @Override
    public Map<OrderColumn, ColumnAlignment> getOrderColumnToAlignment() {
        return groupingContainer.getOrderColumnToAlignment();
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationComponentIdProductInColumnToAlignment() {
        return groupingContainer.getOperationComponentIdProductInColumnToAlignment();
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationComponentIdProductOutColumnToAlignment() {
        return groupingContainer.getOperationComponentIdProductOutColumnToAlignment();
    }

}
