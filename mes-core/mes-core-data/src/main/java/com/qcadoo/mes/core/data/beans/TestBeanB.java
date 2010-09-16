package com.qcadoo.mes.core.data.beans;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private List<TestBeanA> beansA;

    @ManyToOne(fetch = FetchType.LAZY)
    private TestBeanC beanC;

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

    public List<TestBeanA> getBeansA() {
        return beansA;
    }

    public void setBeansA(List<TestBeanA> beansA) {
        this.beansA = beansA;
    }

    public TestBeanC getBeanC() {
        return beanC;
    }

    public void setBeanC(TestBeanC beanC) {
        this.beanC = beanC;
    }

}
