/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orderSupplies.coverage;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.Entity;

public class CoverageProductForOrder extends CoverageProduct {

    private Entity order;

    private Entity technologyOperationComponent;

    private Entity operation;

    private Entity operationProductInComponent;

    private BigDecimal quantity;

    private Map<Long, BigDecimal> productComponentQuantities;

    private OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer;

    public CoverageProductForOrder(final Date coverageDate, final Entity order, final Entity technologyOperationComponent,
            final Entity operation, final Entity operationProductInComponent,
            final Map<Long, BigDecimal> productComponentQuantities,
            final OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer) {
        super(coverageDate);

        this.order = order;
        this.technologyOperationComponent = technologyOperationComponent;
        this.operation = operation;
        this.operationProductInComponent = operationProductInComponent;
        this.productComponentQuantities = productComponentQuantities;
        this.operationProductComponentWithQuantityContainer = operationProductComponentWithQuantityContainer;
    }

    public Entity getOrder() {
        return order;
    }

    public void setOrder(final Entity order) {
        this.order = order;
    }

    public Entity getTechnologyOperationComponent() {
        return technologyOperationComponent;
    }

    public void setTechnologyOperationComponent(final Entity technologyOperationComponent) {
        this.technologyOperationComponent = technologyOperationComponent;
    }

    public Entity getOperation() {
        return operation;
    }

    public void setOperation(final Entity operation) {
        this.operation = operation;
    }

    public Entity getOperationProductInComponent() {
        return operationProductInComponent;
    }

    public void setOperationProductInComponent(final Entity operationProductInComponent) {
        this.operationProductInComponent = operationProductInComponent;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Map<Long, BigDecimal> getProductComponentQuantities() {
        return productComponentQuantities;
    }

    public void setProductComponentQuantities(final Map<Long, BigDecimal> productComponentQuantities) {
        this.productComponentQuantities = productComponentQuantities;
    }

    public OperationProductComponentWithQuantityContainer getOperationProductComponentWithQuantityContainer() {
        return operationProductComponentWithQuantityContainer;
    }

    public void setOperationProductComponentWithQuantityContainer(
            final OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer) {
        this.operationProductComponentWithQuantityContainer = operationProductComponentWithQuantityContainer;
    }

    public Entity getProduct() {
        return operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
    }

    public String getTypeOfProductionRecording() {
        return order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
    }

}