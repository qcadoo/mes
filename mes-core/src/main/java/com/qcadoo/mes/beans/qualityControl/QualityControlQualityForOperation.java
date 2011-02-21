package com.qcadoo.mes.beans.qualityControl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.qcadoo.mes.beans.products.ProductsOperation;
import com.qcadoo.mes.beans.products.ProductsOrder;

@Entity
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "quality_control_quality_operation_sequence")
@Table(name = "quality_control_quality_operation")
public class QualityControlQualityForOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STORE")
    private Long id;

    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsOperation operation;

    private String controlResult;

    private String comment;

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

    public ProductsOperation getOperation() {
        return operation;
    }

    public void setOperation(final ProductsOperation operation) {
        this.operation = operation;
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
