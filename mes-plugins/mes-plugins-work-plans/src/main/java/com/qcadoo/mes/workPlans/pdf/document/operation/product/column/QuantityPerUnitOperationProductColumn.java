/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.TechnologyFieldsO;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Component("quantityPerUnitOperationProductColumn")
public class QuantityPerUnitOperationProductColumn extends AbstractOperationProductColumn {

    private NumberService numberService;

    @Autowired
    public QuantityPerUnitOperationProductColumn(TranslationService translationService, NumberService numberService) {
        super(translationService);
        this.numberService = numberService;
    }

    @Override
    public String getIdentifier() {
        return "quantityPerUnitOperationProductColumn";
    }

    @Override
    public String getColumnValue(Entity operationProduct) {
        List<Entity> orders = operationProduct.getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT)
                .getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY).getHasManyField(TechnologyFieldsO.ORDERS);
        if (orders.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return String.valueOf(numberService.format(numberService.setScale((operationProduct
                .getDecimalField(OperationProductInComponentFields.QUANTITY).divide(
                orders.get(0).getDecimalField(OrderFields.PLANNED_QUANTITY), RoundingMode.HALF_UP)))));
    }

    @Override
    public ProductDirection[] getDirection() {
        return new ProductDirection[] { ProductDirection.IN };
    }
}
