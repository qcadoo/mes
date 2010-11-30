/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.beans.products;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products_material_requirement_component")
public class ProductsMaterialRequirementComponent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private ProductsOrder order;

    @ManyToOne(fetch = FetchType.EAGER)
    private ProductsMaterialRequirement materialRequirement;

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

    public ProductsMaterialRequirement getMaterialRequirement() {
        return materialRequirement;
    }

    public void setMaterialRequirement(final ProductsMaterialRequirement materialRequirement) {
        this.materialRequirement = materialRequirement;
    }

}
