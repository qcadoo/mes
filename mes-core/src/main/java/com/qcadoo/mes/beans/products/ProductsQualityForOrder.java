package com.qcadoo.mes.beans.products;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "products_quality_order")
public class ProductsQualityForOrder {

    @Id
    @GeneratedValue
    private Long id;

    private String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsOrder order;

    private String controlResult;

    private String comment;

    private String controlInstruction;

    private String staff;

    @Temporal(TemporalType.DATE)
    private Date date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductsOrder getOrder() {
        return order;
    }

    public void setOrder(ProductsOrder order) {
        this.order = order;
    }

    public String getControlResult() {
        return controlResult;
    }

    public void setControlResult(String controlResult) {
        this.controlResult = controlResult;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getControlInstruction() {
        return controlInstruction;
    }

    public void setControlInstruction(String controlInstruction) {
        this.controlInstruction = controlInstruction;
    }

    public String getStaff() {
        return staff;
    }

    public void setStaff(String staff) {
        this.staff = staff;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
