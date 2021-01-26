package com.qcadoo.mes.costCalculation.print.dto;

import java.util.Objects;

public class ComponentCostKey {

    private Long productId;

    private String technologyInputProductType;

    public ComponentCostKey(Long productId, String technologyInputProductType) {
        this.productId = productId;
        this.technologyInputProductType = technologyInputProductType;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getTechnologyInputProductType() {
        return technologyInputProductType;
    }

    public void setTechnologyInputProductType(String technologyInputProductType) {
        this.technologyInputProductType = technologyInputProductType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ComponentCostKey that = (ComponentCostKey) o;
        return Objects.equals(productId, that.productId)
                && Objects.equals(technologyInputProductType, that.technologyInputProductType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, technologyInputProductType);
    }
}
