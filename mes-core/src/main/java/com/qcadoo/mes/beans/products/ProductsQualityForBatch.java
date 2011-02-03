package com.qcadoo.mes.beans.products;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.qcadoo.mes.beans.basic.BasicStaff;

@Entity
@Table(name = "products_quality_batch")
public class ProductsQualityForBatch {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsOrder order;

    private String number;

    private BigDecimal batchNr;

    private BigDecimal producedQuantity;

    private BigDecimal rejectedQuantity;

    private BigDecimal acceptedDefectsQuantity;

    private String comment;

    private String controlInstruction;

    @ManyToOne(fetch = FetchType.LAZY)
    private BasicStaff staff;

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

    public BasicStaff getStaff() {
        return staff;
    }

    public void setStaff(BasicStaff staff) {
        this.staff = staff;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(BigDecimal producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public BigDecimal getRejectedQuantity() {
        return rejectedQuantity;
    }

    public void setRejectedQuantity(BigDecimal rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
    }

    public BigDecimal getAcceptedDefectsQuantity() {
        return acceptedDefectsQuantity;
    }

    public void setAcceptedDefectsQuantity(BigDecimal acceptedDefectsQuantity) {
        this.acceptedDefectsQuantity = acceptedDefectsQuantity;
    }

    public BigDecimal getBatchNr() {
        return batchNr;
    }

    public void setBatchNr(BigDecimal batchNr) {
        this.batchNr = batchNr;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
