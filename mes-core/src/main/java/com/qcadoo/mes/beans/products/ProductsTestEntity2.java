package com.qcadoo.mes.beans.products;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

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

    @OneToMany(mappedBy = "testEntity2", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<ProductsTestEntity3> testEntity3;

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

    public List<ProductsTestEntity3> getTestEntity3() {
        return testEntity3;
    }

    public void setTestEntity3(List<ProductsTestEntity3> testEntity3) {
        this.testEntity3 = testEntity3;
    }

}
