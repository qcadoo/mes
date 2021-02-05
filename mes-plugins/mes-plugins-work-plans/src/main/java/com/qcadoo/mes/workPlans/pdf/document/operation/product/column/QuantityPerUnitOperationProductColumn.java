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
package com.qcadoo.mes.workPlans.pdf.document.operation.product.column;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("quantityPerUnitOperationProductColumn")
public class QuantityPerUnitOperationProductColumn extends AbstractOperationProductColumn {

    private NumberService numberService;

    private ParameterService parameterService;

    @Autowired
    public QuantityPerUnitOperationProductColumn(TranslationService translationService, NumberService numberService,
            ParameterService parameterService) {
        super(translationService);
        this.numberService = numberService;
        this.parameterService = parameterService;
    }

    @Override
    public String getIdentifier() {
        return "quantityPerUnitOperationProductColumn";
    }

    @Override
    public String getColumnValue(Entity operationProduct) {
        return "";
    }

    @Override
    public String getColumnValueForOrder(Entity order, Entity operationProduct) {
        Entity parameters = parameterService.getParameter();
        boolean takeActualProgress = parameters.getBooleanField(ParameterFieldsWP.TAKE_ACTUAL_PROGRESS_IN_WORK_PLANS);
        BigDecimal quantity = BigDecimal.ZERO;

        if (takeActualProgress) {
            quantity = operationProduct.getDecimalField("quantity");
        } else {
            quantity = operationProduct.getDecimalField("plannedQuantity");
        }
        return String.valueOf(numberService.format(numberService.setScaleWithDefaultMathContext(quantity.divide(
                order.getDecimalField(OrderFields.PLANNED_QUANTITY), RoundingMode.HALF_UP))));
    }

    @Override
    public ProductDirection[] getDirection() {
        return new ProductDirection[] { ProductDirection.IN };
    }

}
