package com.qcadoo.mes.core.data.beans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Substitute {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String no;

    private String number;

    private String name;

    private String effectiveDateFrom;

    private String effectiveDateTo;

    private boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEffectiveDateFrom() {
        return effectiveDateFrom;
    }

    public void setEffectiveDateFrom(String effectiveDateFrom) {
        this.effectiveDateFrom = effectiveDateFrom;
    }

    public String getEffectiveDateTo() {
        return effectiveDateTo;
    }

    public void setEffectiveDateTo(String effectiveDateTo) {
        this.effectiveDateTo = effectiveDateTo;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
