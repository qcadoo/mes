package com.qcadoo.mes.beans.products;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "products_test_entity_2")
public class ProductsTestEntity2 {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 40)
    private String number;

    @Temporal(TemporalType.DATE)
    private Date dateFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTestEntity1 testEntity1;

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

    public ProductsTestEntity1 getTestEntity1() {
        return testEntity1;
    }

    public void setTestEntity1(ProductsTestEntity1 testEntity1) {
        this.testEntity1 = testEntity1;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setProduct(ProductsProduct product) {
        this.product = product;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

}
