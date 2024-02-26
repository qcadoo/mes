/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.orders.controllers.dto;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OrderHolder {

    private Long id;

    private String number;

    private String name;

    private String description;

    private String state;

    private String typeOfProductionRecording;

    private BigDecimal plannedQuantity;

    private BigDecimal doneQuantity;

    private BigDecimal reportedProductionQuantity;

    private BigDecimal masterOrderQuantity;

    private String masterOrderNumber;

    private String orderCategory;

    private String productionLineNumber;

    private String productNumber;

    private String productName;

    private String productUnit;

    private String companyName;

    private String addressNumber;

    private String dashboardShowForProduct;

    private boolean dashboardShowDescription;

    private String deadline;

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

    public String getTypeOfProductionRecording() {
        return typeOfProductionRecording;
    }

    public void setTypeOfProductionRecording(final String typeOfProductionRecording) {
        this.typeOfProductionRecording = typeOfProductionRecording;
    }

    public String getProductionLineNumber() {
        return productionLineNumber;
    }

    public void setProductionLineNumber(final String productionLineNumber) {
        this.productionLineNumber = productionLineNumber;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(final BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public BigDecimal getDoneQuantity() {
        return doneQuantity;
    }

    public void setDoneQuantity(final BigDecimal doneQuantity) {
        this.doneQuantity = doneQuantity;
    }

    public String getMasterOrderNumber() {
        return masterOrderNumber;
    }

    public void setMasterOrderNumber(final String masterOrderNumber) {
        this.masterOrderNumber = masterOrderNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(final String productNumber) {
        this.productNumber = productNumber;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(final String productUnit) {
        this.productUnit = productUnit;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(final String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public BigDecimal getReportedProductionQuantity() {
        return reportedProductionQuantity;
    }

    public void setReportedProductionQuantity(BigDecimal reportedProductionQuantity) {
        this.reportedProductionQuantity = reportedProductionQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDashboardShowDescription() {
        return dashboardShowDescription;
    }

    public void setDashboardShowDescription(boolean dashboardShowDescription) {
        this.dashboardShowDescription = dashboardShowDescription;
    }

    public String getDashboardShowForProduct() {
        return dashboardShowForProduct;
    }

    public void setDashboardShowForProduct(String dashboardShowForProduct) {
        this.dashboardShowForProduct = dashboardShowForProduct;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(String orderCategory) {
        this.orderCategory = orderCategory;
    }

    public BigDecimal getMasterOrderQuantity() {
        return masterOrderQuantity;
    }

    public void setMasterOrderQuantity(BigDecimal masterOrderQuantity) {
        this.masterOrderQuantity = masterOrderQuantity;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrderHolder that = (OrderHolder) o;

        return new EqualsBuilder().append(id, that.id).append(number, that.number).append(name, that.name)
                .append(description, that.description).append(state, that.state)
                .append(typeOfProductionRecording, that.typeOfProductionRecording).append(plannedQuantity, that.plannedQuantity)
                .append(doneQuantity, that.doneQuantity).append(masterOrderQuantity, that.masterOrderQuantity)
                .append(masterOrderNumber, that.masterOrderNumber).append(orderCategory, that.orderCategory)
                .append(productionLineNumber, that.productionLineNumber).append(productNumber, that.productNumber)
                .append(productName, that.productName).append(productUnit, that.productUnit).append(companyName, that.companyName)
                .append(addressNumber, that.addressNumber).append(dashboardShowForProduct, that.dashboardShowForProduct)
                .append(dashboardShowDescription, that.dashboardShowDescription).append(deadline, that.deadline).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(number).append(name).append(description).append(state)
                .append(typeOfProductionRecording).append(plannedQuantity).append(doneQuantity).append(masterOrderQuantity)
                .append(masterOrderNumber).append(orderCategory).append(productionLineNumber).append(productNumber)
                .append(productName).append(productUnit).append(companyName).append(addressNumber).append(dashboardShowForProduct)
                .append(dashboardShowDescription).append(deadline).toHashCode();
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
