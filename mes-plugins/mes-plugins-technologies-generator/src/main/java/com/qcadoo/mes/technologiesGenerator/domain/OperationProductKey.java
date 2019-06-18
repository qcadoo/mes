package com.qcadoo.mes.technologiesGenerator.domain;

import java.util.Objects;

public class OperationProductKey {

    private Long operationId;

    private Long productId;

    public OperationProductKey(Long operationId, Long productId) {
        this.operationId = operationId;
        this.productId = productId;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OperationProductKey))
            return false;
        OperationProductKey that = (OperationProductKey) o;
        return Objects.equals(operationId, that.operationId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, productId);
    }
}
