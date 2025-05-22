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
package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.materialFlowResources.constants.RepackingPositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public class RepackingPositionDetailsListeners {

    @Autowired
    private NumberService numberService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    @Autowired
    private UnitConversionService unitConversionService;

    public void fillFieldsFromResource(final ViewDefinitionState view, final ComponentState state,
                                       final String[] args) {
        LookupComponent resourceLookup = (LookupComponent) view.getComponentByReference(RepackingPositionFields.RESOURCE);
        FieldComponent resourceNumberField = (FieldComponent) view.getComponentByReference(RepackingPositionFields.RESOURCE_NUMBER);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(RepackingPositionFields.PRODUCT);
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(RepackingPositionFields.STORAGE_LOCATION);
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(RepackingPositionFields.PALLET_NUMBER);
        LookupComponent typeOfLoadUnitLookup = (LookupComponent) view.getComponentByReference(RepackingPositionFields.TYPE_OF_LOAD_UNIT);
        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(RepackingPositionFields.BATCH);

        Entity resource = resourceLookup.getEntity();

        if (resource != null) {
            resourceNumberField.setFieldValue(resource.getStringField(ResourceFields.NUMBER));
            productLookup.setFieldValue(resource.getBelongsToField(ResourceFields.PRODUCT).getId());
            productLookup.requestComponentUpdateState();
            Entity storageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
            if (storageLocation != null) {
                storageLocationLookup.setFieldValue(storageLocation.getId());
            } else {
                storageLocationLookup.setFieldValue(null);
            }
            storageLocationLookup.requestComponentUpdateState();
            Entity palletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
            if (palletNumber != null) {
                palletNumberLookup.setFieldValue(palletNumber.getId());
            } else {
                palletNumberLookup.setFieldValue(null);
            }
            palletNumberLookup.requestComponentUpdateState();
            Entity typeOfLoadUnit = resource.getBelongsToField(ResourceFields.TYPE_OF_LOAD_UNIT);
            if (typeOfLoadUnit != null) {
                typeOfLoadUnitLookup.setFieldValue(typeOfLoadUnit.getId());
            } else {
                typeOfLoadUnitLookup.setFieldValue(null);
            }
            typeOfLoadUnitLookup.requestComponentUpdateState();
            Entity batch = resource.getBelongsToField(ResourceFields.BATCH);
            if (batch != null) {
                batchLookup.setFieldValue(batch.getId());
            } else {
                batchLookup.setFieldValue(null);
            }
            batchLookup.requestComponentUpdateState();
            quantityChange(view, state, args);
        } else {
            resourceNumberField.setFieldValue(null);
            productLookup.setFieldValue(null);
            productLookup.requestComponentUpdateState();
            storageLocationLookup.setFieldValue(null);
            storageLocationLookup.requestComponentUpdateState();
            palletNumberLookup.setFieldValue(null);
            palletNumberLookup.requestComponentUpdateState();
            typeOfLoadUnitLookup.setFieldValue(null);
            typeOfLoadUnitLookup.requestComponentUpdateState();
            batchLookup.setFieldValue(null);
            batchLookup.requestComponentUpdateState();
        }
    }

    public void quantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity repackingPosition = form.getEntity();

        Entity product = repackingPosition.getBelongsToField(RepackingPositionFields.PRODUCT);

        if (decimalFieldsInvalid(form) || Objects.isNull(product)) {
            return;
        }

        BigDecimal quantity = repackingPosition.getDecimalField(RepackingPositionFields.QUANTITY);

        if (Objects.nonNull(quantity)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(
                    unit);

            BigDecimal conversion = getConversion(product, unit, additionalUnit);
            BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity,
                    conversion, additionalUnit);

            FieldComponent additionalQuantityField = (FieldComponent) view
                    .getComponentByReference(RepackingPositionFields.ADDITIONAL_QUANTITY);

            additionalQuantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
            additionalQuantityField.requestComponentUpdateState();

            FieldComponent conversionField = (FieldComponent) view
                    .getComponentByReference(RepackingPositionFields.CONVERSION);

            conversionField.setFieldValue(numberService.formatWithMinimumFractionDigits(conversion, 0));
            conversionField.requestComponentUpdateState();
        }
    }

    public void additionalQuantityChange(final ViewDefinitionState view, final ComponentState state,
                                         final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity repackingPosition = form.getEntity();

        Entity product = repackingPosition.getBelongsToField(RepackingPositionFields.PRODUCT);

        if (decimalFieldsInvalid(form) || Objects.isNull(product)) {
            return;
        }

        BigDecimal additionalQuantity = repackingPosition.getDecimalField(RepackingPositionFields.ADDITIONAL_QUANTITY);

        if (Objects.nonNull(additionalQuantity)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(
                    unit);

            BigDecimal conversion = getConversion(product, unit, additionalUnit);
            BigDecimal quantity = calculationQuantityService.calculateQuantity(additionalQuantity,
                    conversion, unit);

            FieldComponent quantityField = (FieldComponent) view
                    .getComponentByReference(RepackingPositionFields.QUANTITY);

            quantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(quantity, 0));
            quantityField.requestComponentUpdateState();

            FieldComponent conversionField = (FieldComponent) view
                    .getComponentByReference(RepackingPositionFields.CONVERSION);

            conversionField.setFieldValue(numberService.formatWithMinimumFractionDigits(conversion, 0));
            conversionField.requestComponentUpdateState();
        }
    }

    private BigDecimal getConversion(final Entity product, String unit, String additionalUnit) {
        BigDecimal conversion = BigDecimal.ONE;
        if (!unit.equals(additionalUnit)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(additionalUnit)) {
                conversion = unitConversions.asUnitToConversionMap().get(additionalUnit);
            } else {
                conversion = BigDecimal.ZERO;
            }
        }
        return conversion;
    }

    private boolean decimalFieldsInvalid(final FormComponent formComponent) {
        String[] fieldNames = {RepackingPositionFields.ADDITIONAL_QUANTITY,
                RepackingPositionFields.QUANTITY};

        boolean valid = false;

        Entity entity = formComponent.getEntity();

        for (String fieldName : fieldNames) {
            try {
                entity.getDecimalField(fieldName);
            } catch (IllegalArgumentException e) {
                formComponent.findFieldComponentByName(fieldName).addMessage(
                        "qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);

                valid = true;
            }
        }

        return valid;
    }

}
