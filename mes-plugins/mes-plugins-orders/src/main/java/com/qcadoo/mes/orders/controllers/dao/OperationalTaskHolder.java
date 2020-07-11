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
package com.qcadoo.mes.orders.controllers.dao;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OperationalTaskHolder {

    private Long id;

    private String number;

    private String name;

    private String state;

    private String type;

    private BigDecimal plannedQuantity;

    private BigDecimal usedQuantity;

    private String orderNumber;

    private String workstationNumber;

    private String productNumber;

    private String productUnit;

    private String staffName;

    private String orderProductNumber;

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

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(final BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public BigDecimal getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(final BigDecimal usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getWorkstationNumber() {
        return workstationNumber;
    }

    public void setWorkstationNumber(final String workstationNumber) {
        this.workstationNumber = workstationNumber;
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

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(final String staffName) {
        this.staffName = staffName;
    }

    public String getOrderProductNumber() {
        return orderProductNumber;
    }

    public void setOrderProductNumber(final String orderProductNumber) {
        this.orderProductNumber = orderProductNumber;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OperationalTaskHolder that = (OperationalTaskHolder) o;

        return new EqualsBuilder().append(id, that.id).append(number, that.number).append(name, that.name)
                .append(state, that.state).append(type, that.type).append(plannedQuantity, that.plannedQuantity)
                .append(usedQuantity, that.usedQuantity).append(orderNumber, that.orderNumber)
                .append(workstationNumber, that.workstationNumber).append(productNumber, that.productNumber)
                .append(productUnit, that.productUnit).append(staffName, that.staffName)
                .append(orderProductNumber, that.orderProductNumber).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(number).append(name).append(state).append(type).append(plannedQuantity)
                .append(usedQuantity).append(orderNumber).append(workstationNumber).append(productNumber).append(productUnit)
                .append(staffName).append(orderProductNumber).toHashCode();
    }

}
