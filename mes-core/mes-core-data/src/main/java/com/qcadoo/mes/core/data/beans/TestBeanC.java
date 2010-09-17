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

    @OneToMany(mappedBy = "beanC")
    private List<TestBeanB> beansB;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TestBeanA getBeanA() {
        return beanA;
    }

    public void setBeanA(TestBeanA beanA) {
        this.beanA = beanA;
    }

    public List<TestBeanB> getBeansB() {
        return beansB;
    }

    public void setBeansB(List<TestBeanB> beansB) {
        this.beansB = beansB;
    }

}
