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

import java.math.BigDecimal;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "products_order_sequence")
@Table(name = "products_order")
public class ProductsOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STORE")
    private Long id;

    @Column(nullable = false, length = 40)
    private String number;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String state;

    @Temporal(TemporalType.DATE)
    private Date effectiveDateFrom;

    @Temporal(TemporalType.DATE)
    private Date effectiveDateTo;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateFrom;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateTo;

    private String machine;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsProduct product;

    @Column(scale = 3, precision = 10)
    private BigDecimal plannedQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsInstruction defaultInstruction;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsInstruction instruction;

    @Column(scale = 3, precision = 10)
    private BigDecimal doneQuantity;

    private String startWorker;

    private String endWorker;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<ProductsMaterialRequirementComponent> materialRequirements;

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

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
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

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(final Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(final Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(final String machine) {
        this.machine = machine;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setProduct(final ProductsProduct product) {
        this.product = product;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(final BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public ProductsInstruction getDefaultInstruction() {
        return defaultInstruction;
    }

    public void setDefaultInstruction(final ProductsInstruction defaultInstruction) {
        this.defaultInstruction = defaultInstruction;
    }

    public ProductsInstruction getInstruction() {
        return instruction;
    }

    public void setInstruction(final ProductsInstruction instruction) {
        this.instruction = instruction;
    }

    public BigDecimal getDoneQuantity() {
        return doneQuantity;
    }

    public void setDoneQuantity(final BigDecimal doneQuantity) {
        this.doneQuantity = doneQuantity;
    }

    public String getStartWorker() {
        return startWorker;
    }

    public void setStartWorker(final String startWorker) {
        this.startWorker = startWorker;
    }

    public String getEndWorker() {
        return endWorker;
    }

    public void setEndWorker(final String endWorker) {
        this.endWorker = endWorker;
    }

    public List<ProductsMaterialRequirementComponent> getMaterialRequirements() {
        return materialRequirements;
    }

    public void setMaterialRequirements(final List<ProductsMaterialRequirementComponent> materialRequirements) {
        this.materialRequirements = materialRequirements;
    }

}
