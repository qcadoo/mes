package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public final class ProductWithQuantityAndCost {

    private final Long productId;

    private final BigDecimal quantity;

    private final BigDecimal cost;

    public ProductWithQuantityAndCost(Long productId, BigDecimal quantity, BigDecimal cost) {
        this.productId = Objects.requireNonNull(productId);
        this.quantity = quantity;
        this.cost = cost;
    }

    public Long getProductId() {
        return productId;
    }

    public Optional<BigDecimal> getQuantityOpt() {
        return Optional.ofNullable(quantity);
    }

    public Optional<BigDecimal> getCostOpt() {
        return Optional.ofNullable(cost);
    }
}
