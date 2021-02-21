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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Objects;

public class OrderOperationComponent {

    private Entity order;

    private Entity operationComponent;

    private List<Entity> productionCountingQuantitiesIn = Lists.newArrayList();

    private List<Entity> productionCountingQuantitiesOut = Lists.newArrayList();

    public OrderOperationComponent(final Entity order, final Entity operationComponent,
            List<Entity> productionCountingQuantitiesIn, List<Entity> productionCountingQuantitiesOut) {
        this.order = order;
        this.operationComponent = operationComponent;
        this.productionCountingQuantitiesIn = productionCountingQuantitiesIn;
        this.productionCountingQuantitiesOut = productionCountingQuantitiesOut;
    }

    public Entity getOrder() {
        return order;
    }

    public void setOrder(final Entity order) {
        this.order = order;
    }

    public Entity getOperationComponent() {
        return operationComponent;
    }

    public void setOperationComponent(final Entity operationComponent) {
        this.operationComponent = operationComponent;
    }

    public List<Entity> getProductionCountingQuantitiesIn() {
        return productionCountingQuantitiesIn;
    }

    public void setProductionCountingQuantitiesIn(List<Entity> productionCountingQuantitiesIn) {
        this.productionCountingQuantitiesIn = productionCountingQuantitiesIn;
    }

    public List<Entity> getProductionCountingQuantitiesOut() {
        return productionCountingQuantitiesOut;
    }

    public void setProductionCountingQuantitiesOut(List<Entity> productionCountingQuantitiesOut) {
        this.productionCountingQuantitiesOut = productionCountingQuantitiesOut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrderOperationComponent))
            return false;
        OrderOperationComponent that = (OrderOperationComponent) o;
        return Objects.equals(order.getId(), that.order.getId())
                && Objects.equals(operationComponent.getId(), that.operationComponent.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(order.getId(), operationComponent.getId());
    }
}
