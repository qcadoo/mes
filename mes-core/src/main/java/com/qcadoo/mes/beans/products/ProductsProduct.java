/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
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
