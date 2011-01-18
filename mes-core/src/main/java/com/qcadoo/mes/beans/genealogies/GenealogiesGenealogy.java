package com.qcadoo.mes.beans.genealogies;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.qcadoo.mes.beans.products.ProductsOrder;

@Entity
@Table(name = "genealogies_genealogy")
public class GenealogiesGenealogy {

    @Id
    @GeneratedValue
    private Long id;

    private String batch;

    @Column(scale = 3, precision = 10)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsOrder order;

    @OneToMany(mappedBy = "genealogy", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesGenealogyProductInComponent> genealogyProductInComponents;

    @OneToMany(mappedBy = "genealogy", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesGenealogyShiftFeature> shiftFeatures;

    @OneToMany(mappedBy = "genealogy", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesGenealogyOtherFeature> otherFeatures;

    @OneToMany(mappedBy = "genealogy", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesGenealogyPostFeature> postFeatures;

    @Temporal(TemporalType.DATE)
    private Date date;

    private String worker;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(final String batch) {
        this.batch = batch;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public ProductsOrder getOrder() {
        return order;
    }

    public void setOrder(final ProductsOrder order) {
        this.order = order;
    }

    public List<GenealogiesGenealogyProductInComponent> getGenealogyProductInComponents() {
        return genealogyProductInComponents;
    }

    public void setGenealogyProductInComponents(final List<GenealogiesGenealogyProductInComponent> genealogyProductInComponents) {
        this.genealogyProductInComponents = genealogyProductInComponents;
    }

    public List<GenealogiesGenealogyShiftFeature> getShiftFeatures() {
        return shiftFeatures;
    }

    public void setShiftFeatures(final List<GenealogiesGenealogyShiftFeature> shiftFeatures) {
        this.shiftFeatures = shiftFeatures;
    }

    public List<GenealogiesGenealogyOtherFeature> getOtherFeatures() {
        return otherFeatures;
    }

    public void setOtherFeatures(final List<GenealogiesGenealogyOtherFeature> otherFeatures) {
        this.otherFeatures = otherFeatures;
    }

    public List<GenealogiesGenealogyPostFeature> getPostFeatures() {
        return postFeatures;
    }

    public void setPostFeatures(final List<GenealogiesGenealogyPostFeature> postFeatures) {
        this.postFeatures = postFeatures;
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
