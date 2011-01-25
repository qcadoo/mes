/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

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

    private String batch;

    private String lastUsedBatch;

    private Boolean genealogyBatchReq = false;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<ProductsSubstitute> substitutes;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsTechnology> technologies;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsOrder> orders;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsOperationProductInComponent> operationProductInComponents;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductsOperationProductOutComponent> operationProductOutComponents;

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

    public List<ProductsSubstitute> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(final List<ProductsSubstitute> substitutes) {
        this.substitutes = substitutes;
    }

    public List<ProductsTechnology> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(final List<ProductsTechnology> technologies) {
        this.technologies = technologies;
    }

    public List<ProductsOrder> getOrders() {
        return orders;
    }

    public void setOrders(final List<ProductsOrder> orders) {
        this.orders = orders;
    }

    public List<ProductsSubstituteComponent> getSubstituteComponents() {
        return substituteComponents;
    }

    public void setSubstituteComponents(final List<ProductsSubstituteComponent> substituteComponents) {
        this.substituteComponents = substituteComponents;
    }

    public List<ProductsOperationProductInComponent> getOperationProductInComponents() {
        return operationProductInComponents;
    }

    public List<ProductsOperationProductOutComponent> getOperationProductOutComponents() {
        return operationProductOutComponents;
    }

    public void setOperationProductInComponents(final List<ProductsOperationProductInComponent> operationProductInComponents) {
        this.operationProductInComponents = operationProductInComponents;
    }

    public void setOperationProductOutComponents(final List<ProductsOperationProductOutComponent> operationProductOutComponents) {
        this.operationProductOutComponents = operationProductOutComponents;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(final String batch) {
        this.batch = batch;
    }

    public String getLastUsedBatch() {
        return lastUsedBatch;
    }

    public void setLastUsedBatch(final String lastUsedBatch) {
        this.lastUsedBatch = lastUsedBatch;
    }

    public Boolean getGenealogyBatchReq() {
        return genealogyBatchReq;
    }

    public void setGenealogyBatchReq(Boolean genealogyBatchReq) {
        this.genealogyBatchReq = genealogyBatchReq;
    }

}
