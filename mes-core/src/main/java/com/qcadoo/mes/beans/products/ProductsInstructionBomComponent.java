/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.beans.products;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "products_instruction_bom_component")
public class ProductsInstructionBomComponent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsInstruction instruction;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsProduct product;

    @Column(scale = 3, precision = 10)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsInstructionBomComponent parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<ProductsInstructionBomComponent> children;

    @Column(nullable = false)
    private boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductsInstruction getInstruction() {
        return instruction;
    }

    public void setInstruction(ProductsInstruction instruction) {
        this.instruction = instruction;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setProduct(ProductsProduct product) {
        this.product = product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public ProductsInstructionBomComponent getParent() {
        return parent;
    }

    public void setParent(ProductsInstructionBomComponent parent) {
        this.parent = parent;
    }

    public List<ProductsInstructionBomComponent> getChildren() {
        return children;
    }

    public void setChildren(List<ProductsInstructionBomComponent> children) {
        this.children = children;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
