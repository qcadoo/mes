package com.qcadoo.mes.core.data.beans;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TestBeanA {

    @Id
    @GeneratedValue
    private Long id;

    private boolean deleted;

    private String name;

    private Boolean active;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private TestBeanB beanB;

    @ManyToOne(fetch = FetchType.LAZY)
    private TestBeanA beanA;

    @OneToMany(mappedBy = "beanA", fetch = FetchType.LAZY)
    private List<TestBeanC> beansC;

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

    public TestBeanB getBeanB() {
        return beanB;
    }

    public void setBeanB(final TestBeanB beanB) {
        this.beanB = beanB;
    }

    public List<TestBeanC> getBeansC() {
        return beansC;
    }

    public void setBeansC(final List<TestBeanC> beansC) {
        this.beansC = beansC;
    }

    public TestBeanA getBeanA() {
        return beanA;
    }

    public void setBeanA(TestBeanA beanA) {
        this.beanA = beanA;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}
