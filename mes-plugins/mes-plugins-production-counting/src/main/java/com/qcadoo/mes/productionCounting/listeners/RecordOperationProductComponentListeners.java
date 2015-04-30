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
package com.qcadoo.mes.productionCounting.listeners;

import java.math.BigDecimal;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class RecordOperationProductComponentListeners {

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final Set<String> UNIT_COMPONENT_REFERENCES = Sets.newHashSet("plannedQuantityUNIT", "usedQuantityUNIT",
            "givenUnit");

    private static final String L_FORM = "form";

    private static final String L_PRODUCT = "product";

    private static final String L_NAME = "name";

    private static final String L_NUMBER = "number";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity componentEntity = form.getPersistedEntityWithIncludedFormValues();
        Entity productEntity = componentEntity.getBelongsToField(L_PRODUCT);

        fillUnits(view, productEntity);
        fillFieldFromProduct(view, productEntity);
    }

    private void fillUnits(final ViewDefinitionState view, final Entity productEntity) {
        String unit = productEntity.getStringField(ProductFields.UNIT);
        for (String componentReferenceName : UNIT_COMPONENT_REFERENCES) {
            FieldComponent unitComponent = (FieldComponent) view.getComponentByReference(componentReferenceName);
            if (unitComponent != null && StringUtils.isEmpty((String) unitComponent.getFieldValue())) {
                unitComponent.setFieldValue(unit);
                unitComponent.requestComponentUpdateState();
            }
        }
    }

    public void fillFieldFromProduct(final ViewDefinitionState view, final Entity productEntity) {
        view.getComponentByReference(L_NUMBER).setFieldValue(productEntity.getField(L_NUMBER));
        view.getComponentByReference(L_NAME).setFieldValue(productEntity.getField(L_NAME));
    }

    public void calculateQuantity(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity productComponent = form.getPersistedEntityWithIncludedFormValues();

        String givenUnit = productComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);
        Entity product = productComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

        FieldComponent givenQuantityField = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductInComponentFields.GIVEN_QUANTITY);
        if (product == null || givenUnit == null || givenUnit.isEmpty() || givenQuantityField.getFieldValue() == null) {
            return;
        }

        Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils.tryParse(
                (String) givenQuantityField.getFieldValue(), view.getLocale());
        if (maybeQuantity.isRight()) {
            if (maybeQuantity.getRight().isPresent()) {
                BigDecimal givenQuantity = maybeQuantity.getRight().get();
                String baseUnit = product.getStringField(ProductFields.UNIT);
                if (baseUnit.equals(givenUnit)) {
                    productComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, givenQuantity);
                } else {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(givenUnit,
                            searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                                    UnitConversionItemFieldsB.PRODUCT, product)));
                    if (unitConversions.isDefinedFor(baseUnit)) {
                        BigDecimal convertedQuantity = unitConversions.convertTo(givenQuantity, baseUnit);
                        productComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, convertedQuantity);
                    } else {
                        productComponent.addError(
                                productComponent.getDataDefinition().getField(
                                        TrackingOperationProductInComponentFields.GIVEN_QUANTITY),
                                "technologies.operationProductInComponent.validate.error.missingUnitConversion");
                        productComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, null);
                    }
                }

            } else {
                productComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, null);
            }
        } else {
            productComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, null);
        }
        form.setEntity(productComponent);

    }

}
