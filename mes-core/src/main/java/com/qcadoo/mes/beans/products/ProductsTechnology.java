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

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "products_technology_sequence")
@Table(name = "products_technology")
public class ProductsTechnology {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STORE")
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String number;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private ProductsProduct product;

    @Column
    private String description;

    private Boolean master;

    private Boolean batchRequired;

    private Boolean shiftFeatureRequired;

    private Boolean postFeatureRequired;

    private Boolean otherFeatureRequired;

    @Column(nullable = false)
    private String componentQuantityAlgorithm;

    private String qualityControlType;

    @Column(scale = 3, precision = 10)
    private BigDecimal unitSamplingNr;

    private String qualityControlInstruction;

    @OneToMany(mappedBy = "technology", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<ProductsTechnologyOperationComponent> operationComponents;

    @OneToMany(mappedBy = "technology", fetch = FetchType.LAZY)
    private List<ProductsOrder> orders;

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

    public ProductsProduct getProduct() {
        return product;
    }

    public void setProduct(final ProductsProduct product) {
        this.product = product;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean getMaster() {
        return master;
    }

    public void setMaster(final Boolean master) {
        this.master = master;
    }

    public List<ProductsTechnologyOperationComponent> getOperationComponents() {
        return operationComponents;
    }

    public void setOperationComponents(final List<ProductsTechnologyOperationComponent> operationComponents) {
        this.operationComponents = operationComponents;
    }

    public List<ProductsOrder> getOrders() {
        return orders;
    }

    public void setOrders(final List<ProductsOrder> orders) {
        this.orders = orders;
    }

    public Boolean getBatchRequired() {
        return batchRequired;
    }

    public void setBatchRequired(final Boolean batchRequired) {
        this.batchRequired = batchRequired;
    }

    public Boolean getShiftFeatureRequired() {
        return shiftFeatureRequired;
    }

    public void setShiftFeatureRequired(final Boolean shiftFeatureRequired) {
        this.shiftFeatureRequired = shiftFeatureRequired;
    }

    public Boolean getPostFeatureRequired() {
        return postFeatureRequired;
    }

    public void setPostFeatureRequired(final Boolean postFeatureRequired) {
        this.postFeatureRequired = postFeatureRequired;
    }

    public Boolean getOtherFeatureRequired() {
        return otherFeatureRequired;
    }

    public void setOtherFeatureRequired(final Boolean otherFeatureRequired) {
        this.otherFeatureRequired = otherFeatureRequired;
    }

    public String getComponentQuantityAlgorithm() {
        return componentQuantityAlgorithm;
    }

    public void setComponentQuantityAlgorithm(final String componentQuantityAlgorithm) {
        this.componentQuantityAlgorithm = componentQuantityAlgorithm;
    }

    public String getQualityControlType() {
        return qualityControlType;
    }

    public void setQualityControlType(final String qualityControlType) {
        this.qualityControlType = qualityControlType;
    }

    public BigDecimal getUnitSamplingNr() {
        return unitSamplingNr;
    }

    public void setUnitSamplingNr(final BigDecimal unitSamplingNr) {
        this.unitSamplingNr = unitSamplingNr;
    }

    public String getQualityControlInstruction() {
        return qualityControlInstruction;
    }

    public void setQualityControlInstruction(final String qualityControlInstruction) {
        this.qualityControlInstruction = qualityControlInstruction;
    }

}
