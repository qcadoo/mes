package com.qcadoo.mes.masterOrders;

import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.model.api.Entity;

import java.util.Objects;

public class ProductTechnologyKey {

    private Long productId;

    private Entity product;

    private Long technologyId;

    private Entity technology;

    public ProductTechnologyKey(Entity masterOrderProduct) {
        this.productId = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT).getId();
        this.product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        if (Objects.nonNull(masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY))) {
            this.technologyId = masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY).getId();
            this.technology = masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY);
        } else {
            this.technologyId = null;
            this.technology = null;
        }
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getTechnologyId() {
        return technologyId;
    }

    public void setTechnologyId(Long technologyId) {
        this.technologyId = technologyId;
    }

    public Entity getProduct() {
        return product;
    }

    public void setProduct(Entity product) {
        this.product = product;
    }

    public Entity getTechnology() {
        return technology;
    }

    public void setTechnology(Entity technology) {
        this.technology = technology;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProductTechnologyKey))
            return false;
        ProductTechnologyKey that = (ProductTechnologyKey) o;
        return Objects.equals(productId, that.productId) && Objects.equals(technologyId, that.technologyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, technologyId);
    }
}
