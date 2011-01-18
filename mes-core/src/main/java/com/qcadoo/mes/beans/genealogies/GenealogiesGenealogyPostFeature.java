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
@Table(name = "genealogies_post_feature")
public class GenealogiesGenealogyPostFeature {

    @Id
    @GeneratedValue
    private Long id;

    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    private GenealogiesGenealogy genealogy;

    @Temporal(TemporalType.DATE)
    private Date date;

    private String worker;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public GenealogiesGenealogy getGenealogy() {
        return genealogy;
    }

    public void setGenealogy(final GenealogiesGenealogy genealogy) {
        this.genealogy = genealogy;
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
}
