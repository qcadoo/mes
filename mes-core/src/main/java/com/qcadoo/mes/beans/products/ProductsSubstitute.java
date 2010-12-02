/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.beans.products;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "products_substitute")
public class ProductsSubstitute {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsProduct product;

    private Integer priority;

    @Column(nullable = false, length = 40)
    private String number;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "substitute", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<ProductsSubstituteComponent> components;

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
