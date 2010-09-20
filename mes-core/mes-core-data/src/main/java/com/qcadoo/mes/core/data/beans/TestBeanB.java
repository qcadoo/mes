package com.qcadoo.mes.core.data.beans;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TestBeanB {

    @Id
    @GeneratedValue
    private Long id;

    private boolean deleted;

    private String name;

    private String description;

    @OneToMany(mappedBy = "beanB", fetch = FetchType.LAZY)
    private List<TestBeanA> beansA;

    @ManyToOne(fetch = FetchType.LAZY)
    private TestBeanC beanC;

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

    public List<TestBeanA> getBeansA() {
        return beansA;
    }

    public void setBeansA(final List<TestBeanA> beansA) {
        this.beansA = beansA;
    }

    public TestBeanC getBeanC() {
        return beanC;
    }

    public void setBeanC(final TestBeanC beanC) {
        this.beanC = beanC;
    }

}
