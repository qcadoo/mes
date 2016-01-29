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
package com.qcadoo.mes.costCalculation.print.utils;

import java.awt.*;
import java.math.BigDecimal;

import com.lowagie.text.Font;
import com.qcadoo.report.api.FontUtils;

public class CostCalculationMaterial {

    private String productNumber;

    private String unit;

    private BigDecimal productQuantity;

    private BigDecimal costForGivenQuantity;

    private BigDecimal totalCost;

    private BigDecimal toAdd;

    private Font redFont;

    public CostCalculationMaterial(String productNumber, String unit, BigDecimal productQuantity,
            BigDecimal costForGivenQuantity) {
        this.productNumber = productNumber;
        this.unit = unit;
        this.productQuantity = productQuantity;
        this.costForGivenQuantity = costForGivenQuantity;
        this.totalCost = costForGivenQuantity;
        this.toAdd = BigDecimal.ZERO;
        redFont = new Font(FontUtils.getDejavu(), 7);
        redFont.setColor(Color.RED);
    }

    public CostCalculationMaterial(String productNumber, String unit, BigDecimal productQuantity, BigDecimal costForGivenQuantity,
            BigDecimal totalCost, BigDecimal toAdd) {
        this.productNumber = productNumber;
        this.unit = unit;
        this.productQuantity = productQuantity;
        this.costForGivenQuantity = costForGivenQuantity;
        this.totalCost = totalCost;
        this.toAdd = toAdd;
        redFont = new Font(FontUtils.getDejavu(), 7);
        redFont.setColor(Color.RED);

    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
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

    public BigDecimal getCostForGivenQuantity() {
        return costForGivenQuantity;
    }

    public void setCostForGivenQuantity(BigDecimal costForGivenQuantity) {
        this.costForGivenQuantity = costForGivenQuantity;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Font getFont() {
        if (costForGivenQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return redFont;
        } else {
            return FontUtils.getDejavuRegular7Dark();
        }
    }

    public BigDecimal getToAdd() {
        return toAdd;
    }

    public void setToAdd(BigDecimal toAdd) {
        this.toAdd = toAdd;
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

        if (costForGivenQuantity != null ? !costForGivenQuantity.equals(that.costForGivenQuantity)
                : that.costForGivenQuantity != null) {
            return false;
        }
        if (!productNumber.equals(that.productNumber)) {
            return false;
        }
        if (productQuantity != null ? !productQuantity.equals(that.productQuantity) : that.productQuantity != null) {
            return false;
        }
        if (toAdd != null ? !toAdd.equals(that.toAdd) : that.toAdd != null) {
            return false;
        }
        if (totalCost != null ? !totalCost.equals(that.totalCost) : that.totalCost != null) {
            return false;
        }
        if (!unit.equals(that.unit)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = productNumber.hashCode();
        result = 31 * result + unit.hashCode();
        result = 31 * result + (productQuantity != null ? productQuantity.hashCode() : 0);
        result = 31 * result + (costForGivenQuantity != null ? costForGivenQuantity.hashCode() : 0);
        result = 31 * result + (totalCost != null ? totalCost.hashCode() : 0);
        result = 31 * result + (toAdd != null ? toAdd.hashCode() : 0);
        return result;
    }
}
