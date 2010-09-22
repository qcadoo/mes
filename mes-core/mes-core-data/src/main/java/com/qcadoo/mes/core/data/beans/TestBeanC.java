package com.qcadoo.mes.core.data.beans;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TestBeanC {

    @Id
    @GeneratedValue
    private Long id;

    private boolean deleted;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private TestBeanA beanA;

    @OneToMany(mappedBy = "beanC", fetch = FetchType.LAZY)
    private List<TestBeanB> beansB;

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

    public TestBeanA getBeanA() {
        return beanA;
    }

    public void setBeanA(final TestBeanA beanA) {
        this.beanA = beanA;
    }

    public List<TestBeanB> getBeansB() {
        return beansB;
    }

    public void setBeansB(final List<TestBeanB> beansB) {
        this.beansB = beansB;
    }

}
