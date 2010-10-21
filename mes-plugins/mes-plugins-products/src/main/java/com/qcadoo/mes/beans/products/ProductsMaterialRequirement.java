package com.qcadoo.mes.beans.products;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "products_material_requirement")
public class ProductsMaterialRequirement {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Temporal(TemporalType.DATE)
    private Date date;

    private String worker;

    private boolean deleted;

    private boolean generated;

    @OneToMany(mappedBy = "materialRequirement", fetch = FetchType.LAZY)
    private List<ProductsMaterialRequirementComponent> orders;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
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

    public List<ProductsMaterialRequirementComponent> getOrders() {
        return orders;
    }

    public void setOrders(final List<ProductsMaterialRequirementComponent> orders) {
        this.orders = orders;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(final boolean generated) {
        this.generated = generated;
    }

}
