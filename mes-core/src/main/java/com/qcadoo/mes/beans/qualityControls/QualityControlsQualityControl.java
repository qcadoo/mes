package com.qcadoo.mes.beans.qualityControls;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.qcadoo.mes.beans.products.ProductsOperation;
import com.qcadoo.mes.beans.products.ProductsOrder;

@Entity
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "quality_control_quality_control_sequence", initialValue = 1, allocationSize = 1)
@Table(name = "quality_control_quality_control")
public class QualityControlsQualityControl {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STORE")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsOperation operation;

    @Column(nullable = false, unique = true)
    private String number;

    private String controlResult;

    private String batchNr;

    private BigDecimal controlledQuantity;

    private BigDecimal takenForControlQuantity;

    private BigDecimal rejectedQuantity;

    private BigDecimal acceptedDefectsQuantity;

    private String comment;

    private String controlInstruction;

    private String staff;

    @Temporal(TemporalType.DATE)
    private Date date;

    private boolean closed = false;

    private String qualityControlType;

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

    public BigDecimal getControlledQuantity() {
        return controlledQuantity;
    }

    public void setControlledQuantity(final BigDecimal controlledQuantity) {
        this.controlledQuantity = controlledQuantity;
    }

    public BigDecimal getRejectedQuantity() {
        return rejectedQuantity;
    }

    public void setRejectedQuantity(final BigDecimal rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
    }

    public BigDecimal getAcceptedDefectsQuantity() {
        return acceptedDefectsQuantity;
    }

    public void setAcceptedDefectsQuantity(final BigDecimal acceptedDefectsQuantity) {
        this.acceptedDefectsQuantity = acceptedDefectsQuantity;
    }

    public String getBatchNr() {
        return batchNr;
    }

    public void setBatchNr(final String batchNr) {
        this.batchNr = batchNr;
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

    public BigDecimal getTakenForControlQuantity() {
        return takenForControlQuantity;
    }

    public void setTakenForControlQuantity(BigDecimal takenForControlQuantity) {
        this.takenForControlQuantity = takenForControlQuantity;
    }

    public ProductsOperation getOperation() {
        return operation;
    }

    public void setOperation(ProductsOperation operation) {
        this.operation = operation;
    }

    public String getControlResult() {
        return controlResult;
    }

    public void setControlResult(String controlResult) {
        this.controlResult = controlResult;
    }

    public String getQualityControlType() {
        return qualityControlType;
    }

    public void setQualityControlType(String qualityControlType) {
        this.qualityControlType = qualityControlType;
    }
}
