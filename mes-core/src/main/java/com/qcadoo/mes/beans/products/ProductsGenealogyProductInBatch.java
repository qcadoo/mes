package com.qcadoo.mes.beans.products;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products_genealogy_product_in_batch")
public class ProductsGenealogyProductInBatch {

    @Id
    @GeneratedValue
    private Long id;

    private String batch;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsGenealogyProductInComponent productInComponent;

    public Long getId() {
        return id;
    }

    public String getBatch() {
        return batch;
    }

    public ProductsGenealogyProductInComponent getProductInComponent() {
        return productInComponent;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setBatch(final String batch) {
        this.batch = batch;
    }

    public void setProductInComponent(final ProductsGenealogyProductInComponent productInComponent) {
        this.productInComponent = productInComponent;
    }

}
