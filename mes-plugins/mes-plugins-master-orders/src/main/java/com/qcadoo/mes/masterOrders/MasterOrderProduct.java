package com.qcadoo.mes.masterOrders;

import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.List;

public class MasterOrderProduct {

    private boolean createCollectiveOrders;

    private Entity masterOrderProduct;

    private List<Entity> groupedMasterOrderProduct;

    private Entity product;

    private Entity technology;

    private Entity masterOrder;

    private String comments;

    private BigDecimal quantityRemainingToOrder;

    private BigDecimal minStateQuantity;

    private MasterOrderProduct(Builder builder) {
        this.createCollectiveOrders = builder.createCollectiveOrders;
        this.masterOrderProduct = builder.masterOrderProduct;
        this.groupedMasterOrderProduct = builder.groupedMasterOrderProduct;
        this.product = builder.product;
        this.technology = builder.technology;
        this.quantityRemainingToOrder = builder.quantityRemainingToOrder;
        this.masterOrder = builder.masterOrder;
        this.comments = builder.comments;
        this.minStateQuantity = builder.minStateQuantity;
    }

    public static Builder newMasterOrderProduct() {
        return new Builder();
    }

    public boolean isCreateCollectiveOrders() {
        return createCollectiveOrders;
    }

    public void setCreateCollectiveOrders(boolean createCollectiveOrders) {
        this.createCollectiveOrders = createCollectiveOrders;
    }

    public Entity getMasterOrderProduct() {
        return masterOrderProduct;
    }

    public void setMasterOrderProduct(Entity masterOrderProduct) {
        this.masterOrderProduct = masterOrderProduct;
    }

    public List<Entity> getGroupedMasterOrderProduct() {
        return groupedMasterOrderProduct;
    }

    public void setGroupedMasterOrderProduct(List<Entity> groupedMasterOrderProduct) {
        this.groupedMasterOrderProduct = groupedMasterOrderProduct;
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

    public BigDecimal getQuantityRemainingToOrder() {
        return quantityRemainingToOrder;
    }

    public void setQuantityRemainingToOrder(BigDecimal quantityRemainingToOrder) {
        this.quantityRemainingToOrder = quantityRemainingToOrder;
    }

    public Entity getMasterOrder() {
        return masterOrder;
    }

    public void setMasterOrder(Entity masterOrder) {
        this.masterOrder = masterOrder;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public BigDecimal getMinStateQuantity() {
        return minStateQuantity;
    }

    public void setMinStateQuantity(BigDecimal minStateQuantity) {
        this.minStateQuantity = minStateQuantity;
    }

    public static final class Builder {

        private boolean createCollectiveOrders;

        private Entity masterOrderProduct;

        private List<Entity> groupedMasterOrderProduct;

        private Entity product;

        private Entity technology;

        private BigDecimal quantityRemainingToOrder;

        private BigDecimal minStateQuantity;

        private Entity masterOrder;

        private String comments;

        private Builder() {
        }

        public MasterOrderProduct build() {
            return new MasterOrderProduct(this);
        }

        public Builder createCollectiveOrders(boolean createCollectiveOrders) {
            this.createCollectiveOrders = createCollectiveOrders;
            return this;
        }

        public Builder masterOrderProduct(Entity masterOrderProduct) {
            this.masterOrderProduct = masterOrderProduct;
            return this;
        }

        public Builder groupedMasterOrderProduct(List<Entity> groupedMasterOrderProduct) {
            this.groupedMasterOrderProduct = groupedMasterOrderProduct;
            return this;
        }

        public Builder product(Entity product) {
            this.product = product;
            return this;
        }

        public Builder technology(Entity technology) {
            this.technology = technology;
            return this;
        }

        public Builder quantityRemainingToOrder(BigDecimal quantityRemainingToOrder) {
            this.quantityRemainingToOrder = quantityRemainingToOrder;
            return this;
        }

        public Builder minStateQuantity(BigDecimal minStateQuantity) {
            this.minStateQuantity = minStateQuantity;
            return this;
        }

        public Builder masterOrder(Entity masterOrder) {
            this.masterOrder = masterOrder;
            return this;
        }

        public Builder comments(String comments) {
            this.comments = comments;
            return this;
        }
    }
}
