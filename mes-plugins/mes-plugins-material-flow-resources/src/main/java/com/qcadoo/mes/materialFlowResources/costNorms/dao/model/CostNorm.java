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
package com.qcadoo.mes.materialFlowResources.costNorms.dao.model;

import java.math.BigDecimal;

public class CostNorm {

    private Long productId;

    private BigDecimal lastPurchaseCost;

    private BigDecimal averageCost;

    private BigDecimal nominalCost;

    private BigDecimal costForNumber;

    public CostNorm() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getLastPurchaseCost() {
        return lastPurchaseCost;
    }

    public void setLastPurchaseCost(BigDecimal value) {
        this.lastPurchaseCost = value;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getNominalCost() {
        return nominalCost;
    }

    public void setNominalCost(BigDecimal nominalCost) {
        this.nominalCost = nominalCost;
    }

    public BigDecimal getCostForNumber() {
        return costForNumber;
    }

    public void setCostForNumber(BigDecimal costForNumber) {
        this.costForNumber = costForNumber;
    }
}
