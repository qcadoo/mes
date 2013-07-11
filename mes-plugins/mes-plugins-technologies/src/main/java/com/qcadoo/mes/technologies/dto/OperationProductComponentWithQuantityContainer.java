package com.qcadoo.mes.technologies.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;

public class OperationProductComponentWithQuantityContainer {

    private final Map<OperationProductComponentHolder, BigDecimal> operationProductComponentWithQuantities;

    public OperationProductComponentWithQuantityContainer() {
        this.operationProductComponentWithQuantities = Maps.newHashMap();
    }

    public void put(final OperationProductComponentHolder operationProductComponentHolder, BigDecimal quantity) {
        operationProductComponentWithQuantities.put(operationProductComponentHolder, quantity);
    }

    public void put(final Entity operationProductComponent, final BigDecimal quantity) {
        Preconditions.checkNotNull(operationProductComponent, "Missing operationProductComponent entity!");

        OperationProductComponentHolder operationProductComponentHolder = new OperationProductComponentHolder(
                operationProductComponent);

        put(operationProductComponentHolder, quantity);
    }

    public BigDecimal get(final OperationProductComponentHolder operationProductComponentHolder) {
        return operationProductComponentWithQuantities.get(operationProductComponentHolder);
    }

    public BigDecimal get(final Entity operationProductComponent) {
        Preconditions.checkNotNull(operationProductComponent, "Missing operationProductComponent entity!");

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
        Preconditions.checkNotNull(operationProductComponent, "Missing operationProductComponent entity!");

        OperationProductComponentHolder operationProductComponentHolder = new OperationProductComponentHolder(
                operationProductComponent);

        remove(operationProductComponentHolder);
    }

    public boolean containsKey(final OperationProductComponentHolder operationProductComponentHolder) {
        return operationProductComponentWithQuantities.containsKey(operationProductComponentHolder);
    }

    public boolean containsKey(final Entity operationProductComponent) {
        Preconditions.checkNotNull(operationProductComponent, "Missing operationProductComponent entity!");

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
        if (obj == null || !(obj instanceof OperationProductComponentWithQuantityContainer)) {
            return false;
        }
        OperationProductComponentWithQuantityContainer other = (OperationProductComponentWithQuantityContainer) obj;
        return operationProductComponentWithQuantities.equals(other.operationProductComponentWithQuantities);
    }

}
