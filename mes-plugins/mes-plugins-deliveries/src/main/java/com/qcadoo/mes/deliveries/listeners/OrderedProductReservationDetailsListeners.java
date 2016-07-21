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
package com.qcadoo.mes.deliveries.listeners;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductReservationFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.context.i18n.LocaleContextHolder;

@Service
public class OrderedProductReservationDetailsListeners {

    @Autowired
    private NumberService numberService;

    public void quantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity orderedProductReservation = form.getEntity();
        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);
        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);

        Object orderedQuantityRawValue = orderedProductReservation.getField(OrderedProductReservationFields.ORDERED_QUANTITY);
        Either<Exception, Optional<BigDecimal>> tryParseOrderedQuantity = BigDecimalUtils.tryParseAndIgnoreSeparator(orderedQuantityRawValue == null ? "" : orderedQuantityRawValue.toString(), LocaleContextHolder.getLocale());

        if (conversion != null && tryParseOrderedQuantity.isRight() && tryParseOrderedQuantity.getRight().isPresent()) {
            BigDecimal orderedQuantity = tryParseOrderedQuantity.getRight().get();
            FieldComponent additionalQuantity = (FieldComponent) view.getComponentByReference(OrderedProductReservationFields.ADDITIONAL_QUANTITY);
            BigDecimal newAdditionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());
            newAdditionalQuantity = newAdditionalQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL, RoundingMode.HALF_UP);
            additionalQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
            additionalQuantity.requestComponentUpdateState();
        }
    }

    public void additionalQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity orderedProductReservation = form.getEntity();
        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);

        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);
        Object additionalQuantityRawValue = orderedProductReservation.getField(OrderedProductReservationFields.ADDITIONAL_QUANTITY);
        Either<Exception, Optional<BigDecimal>> tryParseAdditionalQuantity = BigDecimalUtils.tryParseAndIgnoreSeparator(additionalQuantityRawValue == null ? "" : additionalQuantityRawValue.toString(), LocaleContextHolder.getLocale());

        if (conversion != null && tryParseAdditionalQuantity.isRight() && tryParseAdditionalQuantity.getRight().isPresent()) {
            BigDecimal additionalQuantity = tryParseAdditionalQuantity.getRight().get();
            FieldComponent orderedQuantity = (FieldComponent) view.getComponentByReference(OrderedProductReservationFields.ORDERED_QUANTITY);
            BigDecimal newOrderedQuantity = additionalQuantity.divide(conversion, numberService.getMathContext());
            newOrderedQuantity = newOrderedQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL, RoundingMode.HALF_UP);
            orderedQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newOrderedQuantity, 0));
            orderedQuantity.requestComponentUpdateState();
        }
    }

}
