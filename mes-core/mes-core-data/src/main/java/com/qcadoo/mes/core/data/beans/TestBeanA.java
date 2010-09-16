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
public class TestBeanA {

    @Id
    @GeneratedValue
    private Long id;

    private boolean deleted;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private TestBeanB beanB;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private List<TestBeanC> beansC;

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

    public TestBeanB getBeanB() {
        return beanB;
    }

    public void setBeanB(TestBeanB beanB) {
        this.beanB = beanB;
    }

    public List<TestBeanC> getBeansC() {
        return beansC;
    }

    public void setBeansC(List<TestBeanC> beansC) {
        this.beansC = beansC;
    }

}
