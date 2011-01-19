package com.qcadoo.mes.beans.products;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products_test_entity_3")
public class ProductsTestEntity3 {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 40)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTestEntity2 testEntity2;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsProduct product;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public ProductsTestEntity2 getTestEntity2() {
        return testEntity2;
    }

    public void setTestEntity2(ProductsTestEntity2 testEntity2) {
        this.testEntity2 = testEntity2;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setProduct(ProductsProduct product) {
        this.product = product;
    }

}
