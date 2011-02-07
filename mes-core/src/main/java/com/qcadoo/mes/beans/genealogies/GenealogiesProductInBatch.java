package com.qcadoo.mes.beans.genealogies;

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
@Table(name = "genealogies_product_in_batch")
public class GenealogiesProductInBatch {

    @Id
    @GeneratedValue
    private Long id;

    private String batch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GenealogiesGenealogyProductInComponent productInComponent;

    @Temporal(TemporalType.DATE)
    private Date date;

    private String worker;

    public Long getId() {
        return id;
    }

    public String getBatch() {
        return batch;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setBatch(final String batch) {
        this.batch = batch;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(final String worker) {
        this.worker = worker;
    }

    public GenealogiesGenealogyProductInComponent getProductInComponent() {
        return productInComponent;
    }

    public void setProductInComponent(final GenealogiesGenealogyProductInComponent productInComponent) {
        this.productInComponent = productInComponent;
    }
}
