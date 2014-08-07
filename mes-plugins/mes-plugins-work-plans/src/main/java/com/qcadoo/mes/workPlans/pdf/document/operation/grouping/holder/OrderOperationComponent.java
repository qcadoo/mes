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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder;

import com.qcadoo.model.api.Entity;

public class OrderOperationComponent {

    private Entity order;
    private Entity operationComponent;

    public OrderOperationComponent(Entity order, Entity operationComponent) {
        this.order = order;
        this.operationComponent = operationComponent;
    }

    public Entity getOrder() {
        return order;
    }

    public void setOrder(Entity order) {
        this.order = order;
    }

    public Entity getOperationComponent() {
        return operationComponent;
    }

    public void setOperationComponent(Entity operationComponent) {
        this.operationComponent = operationComponent;
    }
}
