package com.qcadoo.mes.beans.products;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "products_substitute")
public final class ProductsSubstitute {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsProduct product;

    private Integer priority;

    private String number;

    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveDateFrom;

    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveDateTo;

    @OneToMany(mappedBy = "substitute", fetch = FetchType.LAZY)
    private List<ProductsSubstituteComponent> components;

    private boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(final Integer priority) {
        this.priority = priority;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getEffectiveDateFrom() {
        return effectiveDateFrom;
    }

    public void setEffectiveDateFrom(final Date effectiveDateFrom) {
        this.effectiveDateFrom = effectiveDateFrom;
    }

    public Date getEffectiveDateTo() {
        return effectiveDateTo;
    }

    public void setEffectiveDateTo(final Date effectiveDateTo) {
        this.effectiveDateTo = effectiveDateTo;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setProduct(final ProductsProduct product) {
        this.product = product;
    }

    public List<ProductsSubstituteComponent> getComponents() {
        return components;
    }

    public void setComponents(final List<ProductsSubstituteComponent> components) {
        this.components = components;
    }
}
