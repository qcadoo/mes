package com.qcadoo.mes.beans.products;

import java.util.Date;

import javax.persistence.Column;
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

    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsOrder order;

    private String controlResult;

    private String comment;

    private String controlInstruction;

    private String staff;

    @Temporal(TemporalType.DATE)
    private Date date;

    private boolean closed = false;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ProductsOrder getOrder() {
        return order;
    }

    public void setOrder(final ProductsOrder order) {
        this.order = order;
    }

    public String getControlResult() {
        return controlResult;
    }

    public void setControlResult(final String controlResult) {
        this.controlResult = controlResult;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getControlInstruction() {
        return controlInstruction;
    }

    public void setControlInstruction(final String controlInstruction) {
        this.controlInstruction = controlInstruction;
    }

    public String getStaff() {
        return staff;
    }

    public void setStaff(final String staff) {
        this.staff = staff;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(final boolean closed) {
        this.closed = closed;
    }

}
