/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.basicProductionCounting.listeners;

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCT;
import static com.qcadoo.mes.orders.constants.OrderFields.DONE_QUANTITY;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class BasicProductionCountingDetailsListeners {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillDoneQuantityField(final ViewDefinitionState viewState, final ComponentState triggerState, final String[] args) {
        FormComponent form = (FormComponent) viewState.getComponentByReference(L_FORM);
        FieldComponent producedQuantity = (FieldComponent) viewState.getComponentByReference(PRODUCED_QUANTITY);
        Long basicProductionCountingId = form.getEntityId();

        if (basicProductionCountingId != null) {
            final Entity basicProductionCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(basicProductionCountingId);

            Entity order = basicProductionCounting.getBelongsToField(ORDER);
            Entity product = basicProductionCounting.getBelongsToField(PRODUCT);

            if (order.getBelongsToField(PRODUCT).getId().equals(product.getId())) {
                final String fieldValue = (String) producedQuantity.getFieldValue();
                if (fieldValue == null || fieldValue.isEmpty()) {
                    return;
                }
                try {
                    final BigDecimal doneQuantity = new BigDecimal(fieldValue.replace(",", ".").replace(" ", "")
                            .replace("\u00A0", ""));
                    order.setField(DONE_QUANTITY, doneQuantity);
                    order = order.getDataDefinition().save(order);
                } catch (NumberFormatException ex) {
                    return;
                }
                if (!order.isValid()) {
                    producedQuantity.addMessage(order.getError(DONE_QUANTITY));
                }
            }
        }
    }

}
