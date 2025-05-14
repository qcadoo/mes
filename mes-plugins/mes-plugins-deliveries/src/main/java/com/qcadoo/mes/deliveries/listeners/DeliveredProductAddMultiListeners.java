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

import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveredProductMultiPositionService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.deliveries.helpers.DeliveredMultiProduct;
import com.qcadoo.mes.deliveries.helpers.DeliveredMultiProductContainer;
import com.qcadoo.mes.deliveries.hooks.DeliveredProductAddMultiHooks;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

@Component
public class DeliveredProductAddMultiListeners {

    private static final String L_OFFER = "offer";

    private static final String L_GENERATED = "generated";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    private static final String L_DELIVERIES_DELIVERED_PRODUCT_MULTI_ERROR_INVALID = "deliveries.deliveredProductMulti.error.invalid";

    private static final String L_DELIVERIES_DELIVERED_PRODUCT_MULTI_ERROR_EMPTY_POSITIONS = "deliveries.deliveredProductMulti.error.emptyPositions";

    private static final String L_DELIVERIES_DELIVERED_PRODUCT_MULTI_SUCCESS = "deliveries.deliveredProductMulti.success";

    private static final String L_DELIVERIES_DELIVERED_PRODUCT_MULTI_ERROR_PRODUCT_EXISTS = "deliveries.deliveredProductMulti.error.productExists";

    private static final String L_DELIVERIES_DELIVERED_PRODUCT_MULTI_POSITION_ERROR_LOCATION_REQUIRED = "deliveries.deliveredProductMultiPosition.error.locationRequired";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_OUT_OF_RANGE_TO_SMALL = "qcadooView.validate.field.error.outOfRange.toSmall";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DeliveredProductAddMultiHooks deliveredProductAddMultiHooks;

    @Autowired
    private DeliveredProductMultiPositionService deliveredProductMultiPositionService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    @Autowired
    private DeliveredProductDetailsListeners deliveredProductDetailsListeners;

    public void createDeliveredProducts(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        FormComponent deliveredProductMultiForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity deliveredProductMulti = deliveredProductMultiForm.getPersistedEntityWithIncludedFormValues();

        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        try {
            if (!validate(deliveredProductMulti)) {
                deliveredProductMultiForm.setEntity(deliveredProductMulti);

                view.addMessage(L_DELIVERIES_DELIVERED_PRODUCT_MULTI_ERROR_INVALID, MessageType.FAILURE);
                generatedCheckBox.setChecked(false);

                return;
            }

            List<Entity> deliveredProductMultiPositions = deliveredProductMulti
                    .getHasManyField(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS);

            if (deliveredProductMultiPositions.isEmpty()) {
                view.addMessage(L_DELIVERIES_DELIVERED_PRODUCT_MULTI_ERROR_EMPTY_POSITIONS, MessageType.FAILURE);
                generatedCheckBox.setChecked(false);

                return;
            }

            trySaveDeliveredProducts(deliveredProductMulti, deliveredProductMultiPositions);
            deliveredProductMultiForm.setEntity(deliveredProductMulti);

            if (deliveredProductMulti.isValid()) {
                state.performEvent(view, "save");

                view.addMessage(L_DELIVERIES_DELIVERED_PRODUCT_MULTI_SUCCESS, MessageType.SUCCESS);
                generatedCheckBox.setChecked(true);
            }
        } catch (Exception ex) {
            generatedCheckBox.setChecked(false);
            deliveredProductMultiForm.setEntity(deliveredProductMulti);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trySaveDeliveredProducts(final Entity deliveredProductMulti,
                                         final List<Entity> deliveredProductMultiPositions) {
        Entity delivery = deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.DELIVERY);

        for (Entity position : deliveredProductMultiPositions) {
            Entity deliveredProduct = createDeliveredProduct(position, getDeliveredProductDD());

            setStorageLocationFields(deliveredProduct, deliveredProductMulti);

            deliveredProduct.setField(DeliveredProductFields.DELIVERY, delivery);
            deliveredProduct = deliveredProduct.getDataDefinition().save(deliveredProduct);

            if (!deliveredProduct.isValid()) {
                for (Map.Entry<String, ErrorMessage> entry : deliveredProduct.getErrors().entrySet()) {
                    if (Objects.nonNull(position.getDataDefinition().getField(entry.getKey()))) {
                        position.addError(position.getDataDefinition().getField(entry.getKey()), entry.getValue().getMessage(), entry.getValue().getVars());
                    } else {
                        position.addGlobalError(entry.getValue().getMessage(), false, entry.getValue().getVars());
                    }
                }

                deliveredProductMulti.addGlobalError(L_DELIVERIES_DELIVERED_PRODUCT_MULTI_ERROR_INVALID);

                throw new IllegalStateException("Undone saved delivered product");
            }
        }
    }

    private void setStorageLocationFields(final Entity deliveredProduct, final Entity deliveredProductMulti) {
        deliveredProduct.setField(DeliveredProductFields.PALLET_NUMBER,
                deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.PALLET_NUMBER));
        deliveredProduct.setField(DeliveredProductFields.TYPE_OF_LOAD_UNIT,
                deliveredProductMulti.getField(DeliveredProductMultiFields.TYPE_OF_LOAD_UNIT));
        deliveredProduct.setField(DeliveredProductFields.STORAGE_LOCATION,
                deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.STORAGE_LOCATION));
    }

    private boolean validate(final Entity deliveredProductMulti) {
        if (!deliveryHasLocationSet(deliveredProductMulti)) {
            return false;
        }

        DataDefinition deliveredProductMultiDD = deliveredProductMulti.getDataDefinition();

        Stream.of(DeliveredProductMultiFields.PALLET_NUMBER, DeliveredProductMultiFields.TYPE_OF_LOAD_UNIT,
                DeliveredProductMultiFields.STORAGE_LOCATION).forEach(fieldName -> {
            if (Objects.isNull(deliveredProductMulti.getField(fieldName))) {
                deliveredProductMulti.addError(deliveredProductMultiDD.getField(fieldName),
                        L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            }
        });

        boolean isValid = deliveredProductMulti.isValid();

        DataDefinition deliveredProductMultiPositionDD = getDeliveredProductMultiPositionDD();

        List<Entity> deliveredProductMultiPositions = deliveredProductMulti
                .getHasManyField(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS);

        DeliveredMultiProductContainer multiProductContainer = new DeliveredMultiProductContainer();

        for (Entity position : deliveredProductMultiPositions) {
            checkExpirationDate(deliveredProductMulti, position, DeliveredProductMultiPositionFields.EXPIRATION_DATE,
                    deliveredProductMultiPositionDD);
            checkMissing(position, DeliveredProductMultiPositionFields.PRODUCT, deliveredProductMultiPositionDD);
            checkMissingOrZero(position, DeliveredProductMultiPositionFields.QUANTITY, deliveredProductMultiPositionDD);
            checkMissingOrZero(position, DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY,
                    deliveredProductMultiPositionDD);
            checkMissingOrZero(position, DeliveredProductMultiPositionFields.CONVERSION, deliveredProductMultiPositionDD);

            if (position.isValid()) {
                Entity product = position.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);
                Date expirationDate = position.getDateField(DeliveredProductMultiPositionFields.EXPIRATION_DATE);

                if (multiProductContainer
                        .checkIfExists(new DeliveredMultiProduct(mapToId(product), expirationDate))) {
                    position.addError(deliveredProductMultiPositionDD.getField(DeliveredProductMultiPositionFields.PRODUCT),
                            L_DELIVERIES_DELIVERED_PRODUCT_MULTI_ERROR_PRODUCT_EXISTS);
                } else {
                    DeliveredMultiProduct deliveredMultiProduct = new DeliveredMultiProduct(mapToId(product), expirationDate);

                    multiProductContainer.addProduct(deliveredMultiProduct);
                }
            }

            isValid = isValid && position.isValid();
        }

        return isValid;
    }

    private boolean deliveryHasLocationSet(final Entity deliveredProductMulti) {
        Entity delivery = deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        if (Objects.isNull(location)) {
            deliveredProductMulti.addGlobalError(L_DELIVERIES_DELIVERED_PRODUCT_MULTI_POSITION_ERROR_LOCATION_REQUIRED);

            return false;
        }

        return true;
    }

    private Long mapToId(final Entity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }

        return entity.getId();
    }

    private void checkMissing(final Entity position, final String fieldName,
                              final DataDefinition positionDataDefinition) {
        if (Objects.isNull(position.getField(fieldName))) {
            position.addError(positionDataDefinition.getField(fieldName), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
        }
    }

    private void checkMissingOrZero(final Entity position, final String fieldName,
                                    final DataDefinition positionDataDefinition) {
        if (Objects.isNull(position.getField(fieldName))) {
            position.addError(positionDataDefinition.getField(fieldName), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
        } else if (BigDecimal.ZERO.compareTo(position.getDecimalField(fieldName)) >= 0) {
            position.addError(positionDataDefinition.getField(fieldName),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_OUT_OF_RANGE_TO_SMALL);
        }
    }

    private void checkExpirationDate(final Entity deliveredProductMulti, final Entity position, final String fieldname,
                                     final DataDefinition positionDataDefinition) {
        Entity delivery = deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        if (Objects.nonNull(location)) {
            Date expirationDate = position.getDateField(DeliveredProductMultiPositionFields.EXPIRATION_DATE);

            boolean requireExpirationDate = location.getBooleanField(LocationFieldsMFR.REQUIRE_EXPIRATION_DATE);

            if (requireExpirationDate && Objects.isNull(expirationDate)) {
                position.addError(positionDataDefinition.getField(fieldname), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            }
        }
    }

    private Entity createDeliveredProduct(final Entity position, final DataDefinition deliveredProductDD) {
        Entity deliveredProduct = deliveredProductDD.create();

        deliveredProduct.setField(DeliveredProductFields.PRODUCT,
                position.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT));
        BigDecimal quantity = position.getDecimalField(DeliveredProductMultiPositionFields.QUANTITY);
        deliveredProduct.setField(DeliveredProductFields.DELIVERED_QUANTITY,
                quantity);
        deliveredProduct.setField(DeliveredProductFields.ADDITIONAL_QUANTITY,
                position.getDecimalField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY));
        deliveredProduct.setField(DeliveredProductFields.CONVERSION,
                position.getDecimalField(DeliveredProductMultiPositionFields.CONVERSION));
        deliveredProduct.setField(DeliveredProductFields.IS_WASTE,
                position.getBooleanField(DeliveredProductMultiPositionFields.IS_WASTE));
        deliveredProduct.setField(DeliveredProductFields.BATCH,
                position.getBelongsToField(DeliveredProductMultiPositionFields.BATCH));
        deliveredProduct.setField(DeliveredProductFields.ADDITIONAL_UNIT,
                position.getStringField(DeliveredProductMultiPositionFields.ADDITIONAL_UNIT));
        BigDecimal pricePerUnit = position.getDecimalField(DeliveredProductMultiPositionFields.PRICE_PER_UNIT);
        deliveredProduct.setField(DeliveredProductFields.PRICE_PER_UNIT, pricePerUnit);
        if (pricePerUnit != null) {
            BigDecimal totalPrice = numberService
                    .setScaleWithDefaultMathContext(pricePerUnit.multiply(quantity, numberService.getMathContext()));

            deliveredProduct.setField(DeliveredProductFields.TOTAL_PRICE, totalPrice);
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            if (Objects.nonNull(position.getId())) {
                deliveredProduct.setField(L_OFFER, position.getDataDefinition().get(position.getId()).getBelongsToField(L_OFFER));
            }
        }

        return deliveredProduct;
    }

    public void fillTypeOfLoadUnitField(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PALLET_NUMBER);
        LookupComponent typeOfLoadUnitLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.TYPE_OF_LOAD_UNIT);

        Entity delivery = extractDeliveryEntityFromView(view);

        Entity palletNumber = palletNumberLookup.getEntity();
        Long typeOfLoadUnit = (Long) typeOfLoadUnitLookup.getFieldValue();

        if (Objects.nonNull(palletNumber)) {
            typeOfLoadUnit = deliveredProductDetailsListeners.getTypeLoadUnit(delivery, palletNumber);
        }

        typeOfLoadUnitLookup.setFieldValue(typeOfLoadUnit);
        typeOfLoadUnitLookup.requestComponentUpdateState();
    }

    public void productChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity delivery = extractDeliveryEntityFromView(view);

        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS);

        List<FormComponent> deliveredProductMultiPositionsFormComponents = deliveredProductMultiPositions.getFormComponents();

        for (FormComponent deliveredProductMultiPositionsFormComponent : deliveredProductMultiPositionsFormComponents) {
            Entity deliveredProductMultiPosition = deliveredProductMultiPositionsFormComponent.getEntity();

            Entity product = deliveredProductMultiPosition.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);

            LookupComponent productLookup = (LookupComponent) deliveredProductMultiPositionsFormComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.PRODUCT);
            LookupComponent batchLookup = (LookupComponent) deliveredProductMultiPositionsFormComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.BATCH);
            FieldComponent conversionField = deliveredProductMultiPositionsFormComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.CONVERSION);

            if (productLookup.getUuid().equals(state.getUuid())) {
                recalculateQuantities(delivery, deliveredProductMultiPosition);

                deliveredProductAddMultiHooks.boldRequired(deliveredProductMultiPositionsFormComponent);
                deliveredProductAddMultiHooks.filterBatch(batchLookup, product);

                if (Objects.nonNull(product)) {
                    conversionField.setEnabled(Objects.nonNull(product.getStringField(ProductFields.ADDITIONAL_UNIT)));
                    String unit = product.getStringField(ProductFields.UNIT);
                    deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.UNIT, unit);
                    String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT))
                            .orElse(unit);
                    BigDecimal conversion = deliveriesService.getConversion(product, unit, additionalUnit, null);
                    deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.CONVERSION, numberService.setScaleWithDefaultMathContext(conversion));
                    deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.ADDITIONAL_UNIT,
                            additionalUnit);

                    Entity batch = batchLookup.getEntity();
                    if (Objects.nonNull(batch)) {
                        if (!product.getId().equals(batch.getBelongsToField(BatchFields.PRODUCT).getId())) {
                            deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.BATCH, null);
                        }
                    }

                    deliveredProductMultiPositionsFormComponent.setEntity(deliveredProductMultiPosition);
                }
            }
        }
    }

    private Entity extractDeliveryEntityFromView(final ViewDefinitionState view) {
        FormComponent deliveredProductMultiForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity deliveredProductMulti = deliveredProductMultiForm.getPersistedEntityWithIncludedFormValues();

        return deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.DELIVERY);
    }

    private void recalculateQuantities(final Entity delivery, final Entity deliveredProductMultiPosition) {
        Entity product = deliveredProductMultiPosition.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);

        if (Objects.nonNull(product)) {
            BigDecimal orderedQuantity = deliveredProductMultiPositionService.findOrderedQuantity(delivery, product);
            BigDecimal alreadyAssignedQuantity = deliveredProductMultiPositionService.countAlreadyAssignedQuantityForProduct(
                    product, deliveredProductMultiPosition.getBelongsToField(L_OFFER),
                    delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS));

            BigDecimal quantity = orderedQuantity.subtract(alreadyAssignedQuantity, numberService.getMathContext());

            if (BigDecimal.ZERO.compareTo(quantity) > 0) {
                quantity = BigDecimal.ZERO;
            }

            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT))
                    .orElse(unit);

            BigDecimal conversion = deliveriesService.getConversion(product, unit, additionalUnit, deliveredProductMultiPosition.getDecimalField(DeliveredProductMultiPositionFields.CONVERSION));
            BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity,
                    conversion, additionalUnit);

            deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.QUANTITY, quantity);
            deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY,
                    newAdditionalQuantity);
        }
    }

    public void quantityChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS);

        List<FormComponent> deliveredProductMultiPositionsFormComponents = deliveredProductMultiPositions.getFormComponents();

        for (FormComponent deliveredProductMultiPositionsFormComponent : deliveredProductMultiPositionsFormComponents) {
            Entity deliveredProductMultiPosition = deliveredProductMultiPositionsFormComponent
                    .getPersistedEntityWithIncludedFormValues();

            boolean quantityComponentInForm = state.getUuid().equals(deliveredProductMultiPositionsFormComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.QUANTITY).getUuid());
            boolean conversionComponentInForm = state.getUuid().equals(deliveredProductMultiPositionsFormComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.CONVERSION).getUuid());

            if (quantityComponentInForm || conversionComponentInForm) {
                Entity product = deliveredProductMultiPosition.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);
                BigDecimal quantity = deliveredProductMultiPosition.getDecimalField(DeliveredProductMultiPositionFields.QUANTITY);

                if (Objects.nonNull(quantity) && Objects.nonNull(product)) {
                    String unit = product.getStringField(ProductFields.UNIT);
                    String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT))
                            .orElse(unit);

                    BigDecimal conversion = deliveriesService.getConversion(product, unit, additionalUnit, deliveredProductMultiPosition
                            .getDecimalField(DeliveredProductMultiPositionFields.CONVERSION));
                    BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity,
                            conversion, additionalUnit);

                    FieldComponent additionalQuantityField = deliveredProductMultiPositionsFormComponent
                            .findFieldComponentByName(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY);

                    additionalQuantityField
                            .setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
                    additionalQuantityField.requestComponentUpdateState();
                }

                break;
            }
        }
    }

    public void additionalQuantityChanged(final ViewDefinitionState view, final ComponentState state,
                                          final String[] args) {
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS);

        List<FormComponent> deliveredProductMultiPositionsFormComponents = deliveredProductMultiPositions.getFormComponents();

        for (FormComponent deliveredProductMultiPositionsFormComponent : deliveredProductMultiPositionsFormComponents) {
            Entity deliveredProductMultiPosition = deliveredProductMultiPositionsFormComponent
                    .getPersistedEntityWithIncludedFormValues();

            if (state.getUuid().equals(deliveredProductMultiPositionsFormComponent
                    .findFieldComponentByName(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY).getUuid())) {

                Entity product = deliveredProductMultiPosition.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);
                BigDecimal additionalQuantity = deliveredProductMultiPosition
                        .getDecimalField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY);

                if (Objects.nonNull(additionalQuantity) && Objects.nonNull(product)) {
                    String unit = product.getStringField(ProductFields.UNIT);
                    String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT))
                            .orElse(product.getStringField(ProductFields.UNIT));

                    BigDecimal conversion = deliveriesService.getConversion(product, unit, additionalUnit, deliveredProductMultiPosition.getDecimalField(DeliveredProductMultiPositionFields.CONVERSION));
                    BigDecimal newQuantity = calculationQuantityService.calculateQuantity(additionalQuantity,
                            conversion, unit);

                    FieldComponent quantityField = deliveredProductMultiPositionsFormComponent
                            .findFieldComponentByName(DeliveredProductMultiPositionFields.QUANTITY);

                    quantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(newQuantity, 0));
                    quantityField.requestComponentUpdateState();
                }

                break;
            }
        }
    }

    public void onAddRow(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adlState = (AwesomeDynamicListComponent) state;

        for (FormComponent formComponent : adlState.getFormComponents()) {
            deliveredProductAddMultiHooks.boldRequired(formComponent);
        }

        productChanged(view, state, args);
    }

    private DataDefinition getDeliveredProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
    }

    private DataDefinition getDeliveredProductMultiPositionDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_MULTI_POSITION);
    }

}
