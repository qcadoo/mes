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
package com.qcadoo.mes.productionCounting.listeners;

import java.math.BigDecimal;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TrackingOperationProductInComponentDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_PRODUCT = "product";

    private static final String L_WASTE_USED_ONLY = "wasteUsedOnly";

    private static final String L_WASTE_USED_QUANTITY = "wasteUsedQuantity";

    private static final String L_WASTE_UNIT = "wasteUnit";

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_USED_QUANTITY_UNIT = "usedQuantityUNIT";

    @Autowired
    private NumberService numberService;

    @Autowired
    private TrackingOperationProductComponentDetailsListeners trackingOperationProductComponentDetailsListeners;

    private Entity getFormEntity(final ViewDefinitionState view) {
        return ((FormComponent) view.getComponentByReference(L_FORM)).getPersistedEntityWithIncludedFormValues();
    }

    public void onWasteUsedChange(final ViewDefinitionState view, final ComponentState wasteUsed, final String[] args) {
        CheckBoxComponent wasteUsedOnly = (CheckBoxComponent) view.getComponentByReference(L_WASTE_USED_ONLY);
        FieldComponent wasteUsedQuantity = (FieldComponent) view.getComponentByReference(L_WASTE_USED_QUANTITY);
        FieldComponent wasteUnit = (FieldComponent) view.getComponentByReference(L_WASTE_UNIT);

        if (((CheckBoxComponent) wasteUsed).isChecked()) {
            String productUnit = getFormEntity(view).getBelongsToField(L_PRODUCT).getStringField(ProductFields.UNIT);
            if (StringUtils.isEmpty((CharSequence) wasteUnit.getFieldValue())) {
                wasteUnit.setFieldValue(productUnit);
            }
        } else {
            wasteUsedOnly.setFieldValue(null);
            wasteUsedQuantity.setFieldValue(null);
            wasteUnit.setFieldValue(null);
        }
    }

    public void onWasteUsedOnlyChange(final ViewDefinitionState view, final ComponentState wasteUsedOnly, final String[] args) {
        if (((CheckBoxComponent) wasteUsedOnly).isChecked()) {
            FieldComponent usedQuantity = (FieldComponent) view.getComponentByReference(L_USED_QUANTITY);
            FieldComponent wasteUsedQuantity = (FieldComponent) view.getComponentByReference(L_WASTE_USED_QUANTITY);
            Object usedQuantityValue = usedQuantity.getFieldValue();

            if (usedQuantityValue != null) {
                Either<Exception, Optional<BigDecimal>> parsedDecimal = BigDecimalUtils
                        .tryParseAndIgnoreSeparator(usedQuantityValue.toString(), LocaleContextHolder.getLocale());

                boolean usedQuantityIsOKAndNotZero = parsedDecimal.isRight() && parsedDecimal.getRight().isPresent()
                        && !BigDecimalUtils.valueEquals(parsedDecimal.getRight().get(), BigDecimal.ZERO);

                if (usedQuantityIsOKAndNotZero) {
                    wasteUsedQuantity.setFieldValue(usedQuantityValue);
                    usedQuantity.setFieldValue(BigDecimal.ZERO);
                    trackingOperationProductComponentDetailsListeners.calculateQuantityToGiven(view, usedQuantity,
                            ArrayUtils.EMPTY_STRING_ARRAY);
                }
            }
        }
    }

    public void recalculateUsedQuantity(final ViewDefinitionState view, final ComponentState wasteUsedQuantity,
            final String[] args) {
        Entity entity = getFormEntity(view);

        boolean wasteUsedOnly = entity.getBooleanField(TrackingOperationProductInComponentFields.WASTE_USED_ONLY);
        boolean sameUnits = view.getComponentByReference(L_USED_QUANTITY_UNIT).getFieldValue()
                .equals(view.getComponentByReference(L_WASTE_UNIT).getFieldValue());

        if (!wasteUsedOnly && sameUnits) {
            BigDecimal wasteUsed = entity.getDecimalField(TrackingOperationProductInComponentFields.WASTE_USED_QUANTITY);

            if (wasteUsed != null && !BigDecimalUtils.valueEquals(wasteUsed, BigDecimal.ZERO)) {
                BigDecimal savedUsedQuantity = entity.getDataDefinition().get(entity.getId())
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                if (savedUsedQuantity != null) {
                    BigDecimal newValue = savedUsedQuantity.subtract(wasteUsed, numberService.getMathContext());

                    if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                        newValue = BigDecimal.ZERO;
                    }

                    FieldComponent usedQuantity = (FieldComponent) view.getComponentByReference(L_USED_QUANTITY);
                    usedQuantity.setFieldValue(numberService.format(newValue));
                    trackingOperationProductComponentDetailsListeners.calculateQuantityToGiven(view, usedQuantity,
                            ArrayUtils.EMPTY_STRING_ARRAY);
                }
            }
        }
    }

}
