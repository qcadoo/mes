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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "products_product")
public class ProductsProduct {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 40)
    private String number;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String typeOfMaterial;

    private String ean;

    private String category;

    private String unit;

    private boolean deleted;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsSubstitute> substitutes;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsInstruction> instructions;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsOrder> orders;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsInstructionBomComponent> instructionBomComponents;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsSubstituteComponent> substituteComponents;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getTypeOfMaterial() {
        return typeOfMaterial;
    }

    public void setTypeOfMaterial(final String typeOfMaterial) {
        this.typeOfMaterial = typeOfMaterial;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(final String ean) {
        this.ean = ean;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(final String unit) {
        this.unit = unit;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public List<ProductsSubstitute> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(final List<ProductsSubstitute> substitutes) {
        this.substitutes = substitutes;
    }

    public List<ProductsInstruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(final List<ProductsInstruction> instructions) {
        this.instructions = instructions;
    }

    public List<ProductsOrder> getOrders() {
        return orders;
    }

    public void setOrders(final List<ProductsOrder> orders) {
        this.orders = orders;
    }

    public List<ProductsInstructionBomComponent> getInstructionBomComponents() {
        return instructionBomComponents;
    }

    public void setInstructionBomComponents(final List<ProductsInstructionBomComponent> instructionBomComponents) {
        this.instructionBomComponents = instructionBomComponents;
    }

    public List<ProductsSubstituteComponent> getSubstituteComponents() {
        return substituteComponents;
    }

    public void setSubstituteComponents(final List<ProductsSubstituteComponent> substituteComponents) {
        this.substituteComponents = substituteComponents;
    }

}
