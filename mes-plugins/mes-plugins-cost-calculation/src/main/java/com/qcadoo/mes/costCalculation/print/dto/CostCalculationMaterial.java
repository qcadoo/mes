/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.costCalculation.print.dto;

import java.math.BigDecimal;

public class CostCalculationMaterial {

    private String technologyNumber;

    private String finalProductNumber;

    private String productNumber;

    private String productName;

    private String unit;

    private BigDecimal productQuantity;

    private BigDecimal costPerUnit;

    private BigDecimal costForGivenQuantity;

    private String technologyInputProductType;

    private boolean differentProductsInDifferentSizes;

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(BigDecimal productQuantity) {
        this.productQuantity = productQuantity;
    }

    public BigDecimal getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(BigDecimal costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public BigDecimal getCostForGivenQuantity() {
        return costForGivenQuantity;
    }

    public void setCostForGivenQuantity(BigDecimal costForGivenQuantity) {
        this.costForGivenQuantity = costForGivenQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CostCalculationMaterial that = (CostCalculationMaterial) o;

        if (!productNumber.equals(that.productNumber)) {
            return false;
        }
        if (costForGivenQuantity != null ? !costForGivenQuantity.equals(that.costForGivenQuantity)
                : that.costForGivenQuantity != null) {
            return false;
        }
        if (productQuantity != null ? !productQuantity.equals(that.productQuantity) : that.productQuantity != null) {
            return false;
        }
        if (!unit.equals(that.unit)) {
            return false;
        }
        if (!technologyInputProductType.equals(that.technologyInputProductType)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = productNumber.hashCode();
        result = 31 * result + (costForGivenQuantity != null ? costForGivenQuantity.hashCode() : 0);
        result = 31 * result + (productQuantity != null ? productQuantity.hashCode() : 0);
        result = 31 * result + unit.hashCode();
        result = 31 * result + technologyInputProductType.hashCode();
        return result;
    }

    public String getTechnologyInputProductType() {
        return technologyInputProductType;
    }

    public void setTechnologyInputProductType(String technologyInputProductType) {
        this.technologyInputProductType = technologyInputProductType;
    }

    public boolean isDifferentProductsInDifferentSizes() {
        return differentProductsInDifferentSizes;
    }

    public void setDifferentProductsInDifferentSizes(boolean differentProductsInDifferentSizes) {
        this.differentProductsInDifferentSizes = differentProductsInDifferentSizes;
    }

    public String getTechnologyNumber() {
        return technologyNumber;
    }

    public void setTechnologyNumber(String technologyNumber) {
        this.technologyNumber = technologyNumber;
    }

    public String getFinalProductNumber() {
        return finalProductNumber;
    }

    public void setFinalProductNumber(String finalProductNumber) {
        this.finalProductNumber = finalProductNumber;
    }
}
