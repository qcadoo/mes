package com.qcadoo.mes.beans.products;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "plugins_test_bean_a")
public final class ProductsTestBeanA {

    @Id
    @GeneratedValue
    private Long id;

    private boolean deleted;

    private String name;

    private Boolean active;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTestBeanB beanB;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTestBeanA beanA;

    @OneToMany(mappedBy = "beanA", fetch = FetchType.LAZY)
    private List<ProductsTestBeanC> beansC;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public ProductsTestBeanB getBeanB() {
        return beanB;
    }

    public void setBeanB(final ProductsTestBeanB beanB) {
        this.beanB = beanB;
    }

    public List<ProductsTestBeanC> getBeansC() {
        return beansC;
    }

    public void setBeansC(final List<ProductsTestBeanC> beansC) {
        this.beansC = beansC;
    }

    public ProductsTestBeanA getBeanA() {
        return beanA;
    }

    public void setBeanA(final ProductsTestBeanA beanA) {
        this.beanA = beanA;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

}
