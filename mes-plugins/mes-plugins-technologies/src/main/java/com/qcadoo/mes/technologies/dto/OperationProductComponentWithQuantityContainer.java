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
package com.qcadoo.mes.technologies.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;

public class OperationProductComponentWithQuantityContainer {

    private static final String L_MISSING_OPERATION_PRODUCT_COMPONENT_ENTITY = "Missing operationProductComponent entity!";

    private final Map<OperationProductComponentHolder, BigDecimal> operationProductComponentWithQuantities;

    public OperationProductComponentWithQuantityContainer() {
        this.operationProductComponentWithQuantities = Maps.newHashMap();
    }

    public void put(final OperationProductComponentHolder operationProductComponentHolder, BigDecimal quantity) {
        operationProductComponentWithQuantities.put(operationProductComponentHolder, quantity);
    }

    public void put(final Entity operationProductComponent, final BigDecimal quantity) {
        Preconditions.checkNotNull(operationProductComponent, L_MISSING_OPERATION_PRODUCT_COMPONENT_ENTITY);

        OperationProductComponentHolder operationProductComponentHolder = new OperationProductComponentHolder(
                operationProductComponent);

        put(operationProductComponentHolder, quantity);
    }

    public BigDecimal get(final OperationProductComponentHolder operationProductComponentHolder) {
        return operationProductComponentWithQuantities.get(operationProductComponentHolder);
    }

    public BigDecimal get(final Entity operationProductComponent) {
        Preconditions.checkNotNull(operationProductComponent, L_MISSING_OPERATION_PRODUCT_COMPONENT_ENTITY);

        OperationProductComponentHolder operationProductComponentHolder = new OperationProductComponentHolder(
                operationProductComponent);

        return get(operationProductComponentHolder);
    }

    public void remove(final OperationProductComponentHolder operationProductComponentHolder) {
        if (containsKey(operationProductComponentHolder)) {
            operationProductComponentWithQuantities.remove(operationProductComponentHolder);
        }
    }

    public void remove(final Entity operationProductComponent) {
        Preconditions.checkNotNull(operationProductComponent, L_MISSING_OPERATION_PRODUCT_COMPONENT_ENTITY);

        OperationProductComponentHolder operationProductComponentHolder = new OperationProductComponentHolder(
                operationProductComponent);

        remove(operationProductComponentHolder);
    }

    public boolean containsKey(final OperationProductComponentHolder operationProductComponentHolder) {
        return operationProductComponentWithQuantities.containsKey(operationProductComponentHolder);
    }

    public boolean containsKey(final Entity operationProductComponent) {
        Preconditions.checkNotNull(operationProductComponent, L_MISSING_OPERATION_PRODUCT_COMPONENT_ENTITY);

        OperationProductComponentHolder operationProductComponentHolder = new OperationProductComponentHolder(
                operationProductComponent);

        return containsKey(operationProductComponentHolder);
    }

    public Map<OperationProductComponentHolder, BigDecimal> asMap() {
        return Collections.unmodifiableMap(operationProductComponentWithQuantities);
    }

    public OperationProductComponentWithQuantityContainer getAllWithSameEntityType(final String operationProductComponentModelName) {
        return getAllWithSameEntityType(OperationProductComponentEntityType.parseString(operationProductComponentModelName));
    }

    private OperationProductComponentWithQuantityContainer getAllWithSameEntityType(
            final OperationProductComponentEntityType operationProductComponentEntityType) {
        OperationProductComponentWithQuantityContainer allWithSameType = new OperationProductComponentWithQuantityContainer();

        for (Entry<OperationProductComponentHolder, BigDecimal> operationProductComponentWithQuantity : operationProductComponentWithQuantities
                .entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = operationProductComponentWithQuantity.getKey();

            if (operationProductComponentHolder.isEntityTypeSame(operationProductComponentEntityType)) {
                BigDecimal quantity = operationProductComponentWithQuantity.getValue();

                allWithSameType.put(operationProductComponentHolder, quantity);
            }
        }

        return allWithSameType;
    }

    @Override
    public int hashCode() {
        return operationProductComponentWithQuantities.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OperationProductComponentWithQuantityContainer)) {
            return false;
        }

        OperationProductComponentWithQuantityContainer other = (OperationProductComponentWithQuantityContainer) obj;

        return operationProductComponentWithQuantities.equals(other.operationProductComponentWithQuantities);
    }

}
