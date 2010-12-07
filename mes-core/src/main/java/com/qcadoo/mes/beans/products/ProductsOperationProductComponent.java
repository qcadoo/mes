package com.qcadoo.mes.beans.products;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products_operation_product_component")
public class ProductsOperationProductComponent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTechnologyOperationComponent operationComponent;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsProduct product;

    @Column(scale = 3, precision = 10)
    private BigDecimal quantity;

    private boolean inParameter = true;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ProductsTechnologyOperationComponent getOperationComponent() {
        return operationComponent;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setOperationComponent(final ProductsTechnologyOperationComponent operationComponent) {
        this.operationComponent = operationComponent;
    }

    public void setProduct(final ProductsProduct product) {
        this.product = product;
    }

    public boolean isInParameter() {
        return inParameter;
    }

    public void setInParameter(final boolean inParameter) {
        this.inParameter = inParameter;
    }

}
