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
@Table(name = "plugins_test_bean_b")
public class ProductsTestBeanB {

    @Id
    @GeneratedValue
    private Long id;

    private boolean deleted;

    private String name;

    private String description;

    @OneToMany(mappedBy = "beanB", fetch = FetchType.LAZY)
    private List<ProductsTestBeanA> beansA;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTestBeanC beanC;

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

    public List<ProductsTestBeanA> getBeansA() {
        return beansA;
    }

    public void setBeansA(final List<ProductsTestBeanA> beansA) {
        this.beansA = beansA;
    }

    public ProductsTestBeanC getBeanC() {
        return beanC;
    }

    public void setBeanC(final ProductsTestBeanC beanC) {
        this.beanC = beanC;
    }

}
