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
package com.qcadoo.mes.techSubcontracting.hooks;

import com.qcadoo.mes.techSubcontracting.constants.OrderExternalServiceCostFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class OrderExternalServiceCostHooks {

    @Autowired
    private NumberService numberService;

    public boolean validatesWith(final DataDefinition orderExternalServiceCostDD, final Entity orderExternalServiceCost) {
        boolean isValid = true;

        if (Objects.nonNull(orderExternalServiceCost.getId())) {
            BigDecimal quantity = BigDecimalUtils.convertNullToZero(orderExternalServiceCost.getDecimalField(OrderExternalServiceCostFields.QUANTITY));
            BigDecimal unitCost = BigDecimalUtils.convertNullToZero(orderExternalServiceCost.getDecimalField(OrderExternalServiceCostFields.UNIT_COST));
            BigDecimal totalCost = BigDecimalUtils.convertNullToZero(orderExternalServiceCost.getDecimalField(OrderExternalServiceCostFields.TOTAL_COST));

            if ((BigDecimal.ZERO.compareTo(quantity) == 0) && ((BigDecimal.ZERO.compareTo(unitCost) < 0) || (BigDecimal.ZERO.compareTo(totalCost) < 0))) {
                orderExternalServiceCost.addError(orderExternalServiceCostDD.getField(OrderExternalServiceCostFields.QUANTITY), "qcadooView.validate.field.error.missing");

                isValid = false;
            }
        }

        return isValid;
    }

    public void onCreate(final DataDefinition orderExternalServiceCostDD, final Entity orderExternalServiceCost) {
        setIsAddedManually(orderExternalServiceCost);
    }

    public void onSave(final DataDefinition orderExternalServiceCostDD, final Entity orderExternalServiceCost) {
        setTotalCost(orderExternalServiceCost);
    }

    private void setTotalCost(final Entity orderExternalServiceCost) {
        if (checkIfShouldCalculateTotalCost(orderExternalServiceCost)) {
            BigDecimal quantity = BigDecimalUtils.convertNullToZero(orderExternalServiceCost.getDecimalField(OrderExternalServiceCostFields.QUANTITY));
            BigDecimal unitCost = BigDecimalUtils.convertNullToZero(orderExternalServiceCost.getDecimalField(OrderExternalServiceCostFields.UNIT_COST));
            BigDecimal totalCost = quantity.multiply(unitCost, numberService.getMathContext());

            orderExternalServiceCost.setField(OrderExternalServiceCostFields.TOTAL_COST, numberService.setScaleWithDefaultMathContext(totalCost));
        }
    }

    private static boolean checkIfShouldCalculateTotalCost(final Entity orderExternalServiceCost) {
        BigDecimal totalCost = orderExternalServiceCost.getDecimalField(OrderExternalServiceCostFields.TOTAL_COST);

        return Objects.isNull(totalCost) || (BigDecimal.ZERO.compareTo(totalCost) == 0);
    }

    private void setIsAddedManually(final Entity orderExternalServiceCost) {
        if (Objects.isNull(orderExternalServiceCost.getField(OrderExternalServiceCostFields.IS_ADDED_MANUALLY))) {
            orderExternalServiceCost.setField(OrderExternalServiceCostFields.IS_ADDED_MANUALLY, true);
        }
    }

    public boolean onDelete(final DataDefinition orderExternalServiceCostDD, final Entity orderExternalServiceCost) {
        return checkIfIsAddedManually(orderExternalServiceCost);
    }

    private boolean checkIfIsAddedManually(final Entity orderExternalServiceCost) {
        boolean isAddedManually = orderExternalServiceCost.getBooleanField(OrderExternalServiceCostFields.IS_ADDED_MANUALLY);

        if (!isAddedManually) {
            orderExternalServiceCost.addGlobalError("techSubcontracting.orderExternalServiceCost.message.isNotAddedManually");

            return false;
        }

        return true;
    }

}
