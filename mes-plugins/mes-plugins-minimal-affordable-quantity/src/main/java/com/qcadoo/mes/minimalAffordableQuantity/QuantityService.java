/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.minimalAffordableQuantity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class QuantityService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void checkMinimalAffordableQuantity(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {

        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("plannedQuantity");
        if (plannedQuantity == null || "".equals(plannedQuantity.getFieldValue())) {
            return;
        }
        if (technologyLookup.getFieldValue() != null) {
            Entity technology = dataDefinitionService.get("technologies", "technology").get(
                    (Long) technologyLookup.getFieldValue());

            if (technology == null || technology.getId() == null) {
                return;
            }
            Entity technologyEntity = dataDefinitionService.get("technologies", "technology").get(technology.getId());
            if (technologyEntity.getField("minimalQuantity") == null) {
                return;
            } else {
                checkMinimalQuantityValue(viewDefinitionState, technologyEntity);
            }
        }
    }

    private void checkMinimalQuantityValue(final ViewDefinitionState view, final Entity technologyEntity) {
        FieldComponent plannedQuantityComponent = (FieldComponent) view.getComponentByReference("plannedQuantity");
        BigDecimal plannedQuantity = getBigDecimalFromField(plannedQuantityComponent.getFieldValue(), view.getLocale());
        BigDecimal minimalQuantity = getBigDecimalFromField(technologyEntity.getField("minimalQuantity"), view.getLocale());
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                technologyEntity.getBelongsToField("product").getId());
        String unit = product.getStringField("unit");

        if (plannedQuantity.compareTo(minimalQuantity) < 0) {
            ComponentState form = (ComponentState) view.getComponentByReference("form");
            form.addMessage("orders.order.report.minimalQuantity", MessageType.INFO, false, minimalQuantity.toString(), unit);
        }
    }

    private BigDecimal getBigDecimalFromField(final Object value, final Locale locale) {
        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);
            return new BigDecimal(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
