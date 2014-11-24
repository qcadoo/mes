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
package com.qcadoo.mes.workPlans.pdf.document.order.column;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("plannedQuantityOrderColumn")
public class PlannedQuantityOrderColumn extends AbstractOrderColumn {

    private NumberService numberService;

    @Autowired
    public PlannedQuantityOrderColumn(TranslationService translationService, NumberService numberService) {
        super(translationService);
        this.numberService = numberService;
    }

    @Override
    public String getIdentifier() {
        return "plannedQuantityOrderColumn";
    }

    @Override
    public String getColumnValue(Entity order) {
        return plannedQuantity(order) == null ? "-" : numberService.format(plannedQuantity(order)) + " " + productUnit(order);
    }

    private Object plannedQuantity(Entity order) {
        return order.getField(OrderFields.PLANNED_QUANTITY);
    }

    private String productUnit(Entity order) {
        return order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT);
    }
}
