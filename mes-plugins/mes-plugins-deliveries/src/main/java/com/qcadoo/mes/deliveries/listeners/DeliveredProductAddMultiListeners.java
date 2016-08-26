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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveredProductMultiPositionService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiPositionFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.helpers.DeliveredMultiProduct;
import com.qcadoo.mes.deliveries.helpers.DeliveredMultiProductContainer;
import com.qcadoo.mes.deliveries.hooks.DeliveredProductAddMultiHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Component
public class DeliveredProductAddMultiListeners {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveredProductAddMultiHooks deliveredProductAddMultiHooks;

    @Autowired
    private DeliveredProductMultiPositionService deliveredProductMultiPositionService;

    public void createDeliveriedProducts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity deliveredProductMulti = form.getPersistedEntityWithIncludedFormValues();
        try {

            if (!validate(deliveredProductMulti)) {
                form.setEntity(deliveredProductMulti);
                view.addMessage("deliveries.deliveredProductMulti.error.invalid", MessageType.FAILURE);
                return;
            }

            Entity delivery = deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.DELIVERY);
            EntityList deliveredProductMultiPositions = deliveredProductMulti
                    .getHasManyField(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS);
            if (deliveredProductMultiPositions.isEmpty()) {
                view.addMessage("deliveries.deliveredProductMulti.error.emptyPositions", MessageType.FAILURE);
                return;
            }

            trySaveDeliveredProducts(view, deliveredProductMulti, delivery, deliveredProductMultiPositions);
            form.setEntity(deliveredProductMulti);

            if (deliveredProductMulti.isValid()) {
                state.performEvent(view, "save");
                view.addMessage("deliveries.deliveredProductMulti.success", MessageType.SUCCESS);

            }
        } catch (Exception ex) {
            form.setEntity(deliveredProductMulti);
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trySaveDeliveredProducts(final ViewDefinitionState view, final Entity deliveredProductMulti,
            final Entity delivery, final EntityList deliveredProductMultiPositions) {

        DataDefinition deliveredProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
        List<Entity> deliveredProducts = Lists.newArrayList(delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS));

        for (Entity position : deliveredProductMultiPositions) {
            Entity deliveredProduct = createDeliveredProduct(position, deliveredProductDD);
            setStorageLocationFields(deliveredProduct, deliveredProductMulti);
            deliveredProduct.setField(DeliveredProductFields.DELIVERY, delivery);
            deliveredProduct = deliveredProductDD.save(deliveredProduct);
            if (!deliveredProduct.isValid()) {
                for (Map.Entry<String, ErrorMessage> entry : deliveredProduct.getErrors().entrySet()) {
                    if (position.getDataDefinition().getField(entry.getKey()) != null) {
                        position.addError(position.getDataDefinition().getField(entry.getKey()), entry.getValue().getMessage());
                    } else {
                        position.addGlobalError(entry.getValue().getMessage(), false);
                    }
                }
                deliveredProductMulti.addGlobalError("deliveries.deliveredProductMulti.error.invalid");
                throw new IllegalStateException("Undone saved delivered product");
            }
            deliveredProducts.add(deliveredProduct);
        }
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS, deliveredProducts);
        delivery.getDataDefinition().save(delivery);

    }

    private boolean validate(Entity deliveredProductMulti) {
        DataDefinition dataDefinition = deliveredProductMulti.getDataDefinition();
        boolean isValid = true;
        Arrays.asList(DeliveredProductMultiFields.PALLET_NUMBER, DeliveredProductMultiFields.PALLET_TYPE,
                DeliveredProductMultiFields.STORAGE_LOCATION).stream().forEach(f -> {
                    if (deliveredProductMulti.getField(f) == null) {
                        deliveredProductMulti.addError(dataDefinition.getField(f), "qcadooView.validate.field.error.missing");
                    }
                });
        isValid = deliveredProductMulti.isValid();

        DataDefinition positionDataDefinition = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_MULTI_POSITION);
        EntityList deliveredProductMultiPositions = deliveredProductMulti
                .getHasManyField(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS);
        Multimap<Long, Date> positionsMap = ArrayListMultimap.create();
        DeliveredMultiProductContainer multiProductContainer = new DeliveredMultiProductContainer();
        for (Entity position : deliveredProductMultiPositions) {
            checkMissing(position, DeliveredProductMultiPositionFields.PRODUCT, positionDataDefinition);
            checkMissingOrZero(position, DeliveredProductMultiPositionFields.QUANTITY, positionDataDefinition);
            checkMissingOrZero(position, DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY, positionDataDefinition);
            checkMissingOrZero(position, DeliveredProductMultiPositionFields.CONVERSION, positionDataDefinition);
            if (position.isValid()) {
                Entity product = position.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);
                Entity additionalCode = position.getBelongsToField(DeliveredProductMultiPositionFields.ADDITIONAL_CODE);
                Date expirationDate = position.getDateField(DeliveredProductMultiPositionFields.EXPIRATION_DATE);
                if (multiProductContainer
                        .checkIfExsists(new DeliveredMultiProduct(mapToId(product), mapToId(additionalCode), expirationDate))) {
                    position.addError(positionDataDefinition.getField(DeliveredProductMultiPositionFields.PRODUCT),
                            "deliveries.deliveredProductMulti.error.productExists");
                } else {
                    DeliveredMultiProduct deliveredMultiProduct = new DeliveredMultiProduct(mapToId(product),
                            mapToId(additionalCode), expirationDate);
                    multiProductContainer.addProduct(deliveredMultiProduct);
                }
            }
            isValid = isValid && position.isValid();
        }
        return isValid;
    }

    private Long mapToId(Entity entity) {
        if (entity == null) {
            return null;
        }
        return entity.getId();
    }

    private void checkMissing(Entity position, String fieldname, DataDefinition positionDataDefinition) {
        if (position.getField(fieldname) == null) {
            position.addError(positionDataDefinition.getField(fieldname), "qcadooView.validate.field.error.missing");
        }
    }

    private void checkMissingOrZero(Entity position, String fieldname, DataDefinition positionDataDefinition) {
        if (position.getField(fieldname) == null) {
            position.addError(positionDataDefinition.getField(fieldname), "qcadooView.validate.field.error.missing");
        } else if (BigDecimal.ZERO.compareTo(position.getDecimalField(fieldname)) >= 0) {
            position.addError(positionDataDefinition.getField(fieldname), "qcadooView.validate.field.error.outOfRange.toSmall");
        }
    }

    private Entity createDeliveredProduct(Entity position, DataDefinition deliveredProductDD) {
        Entity deliveredProduct = deliveredProductDD.create();
        deliveredProduct.setField(DeliveredProductFields.PRODUCT,
                position.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT));
        deliveredProduct.setField(DeliveredProductFields.DELIVERED_QUANTITY,
                position.getDecimalField(DeliveredProductMultiPositionFields.QUANTITY));
        deliveredProduct.setField(DeliveredProductFields.ADDITIONAL_QUANTITY,
                position.getDecimalField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY));
        deliveredProduct.setField(DeliveredProductFields.CONVERSION,
                position.getDecimalField(DeliveredProductMultiPositionFields.CONVERSION));
        deliveredProduct.setField(DeliveredProductFields.IS_WASTE,
                position.getBooleanField(DeliveredProductMultiPositionFields.IS_WASTE));
        deliveredProduct.setField(DeliveredProductFields.ADDITIONAL_UNIT,
                position.getStringField(DeliveredProductMultiPositionFields.ADDITIONAL_UNIT));
        deliveredProduct.setField(DeliveredProductFields.ADDITIONAL_CODE,
                position.getBelongsToField(DeliveredProductMultiPositionFields.ADDITIONAL_CODE));
        return deliveredProduct;
    }

    private void setStorageLocationFields(Entity deliveredProduct, Entity deliveredProductMulti) {
        deliveredProduct.setField(DeliveredProductFields.PALLET_NUMBER,
                deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.PALLET_NUMBER));
        deliveredProduct.setField(DeliveredProductFields.PALLET_TYPE,
                deliveredProductMulti.getField(DeliveredProductMultiFields.PALLET_TYPE));
        deliveredProduct.setField(DeliveredProductFields.STORAGE_LOCATION,
                deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.STORAGE_LOCATION));
    }

    public void additionalCodeChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity delivery = extractDeliveryEntityFromView(view);
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference("deliveredProductMultiPositions");
        List<FormComponent> formComponents = deliveredProductMultiPositions.getFormComponents();
        for (FormComponent formComponent : formComponents) {
            Entity formEntity = formComponent.getEntity();
            LookupComponent additionalCodeComponent = (LookupComponent) formComponent.findFieldComponentByName("additionalCode");
            if (additionalCodeComponent.getUuid().equals(state.getUuid())) {
                recalculateQuantities(delivery, formEntity);

                FieldComponent quantityComponent = formComponent.findFieldComponentByName("quantity");
                quantityComponent.setFieldValue(numberService
                        .formatWithMinimumFractionDigits(formEntity.getField(DeliveredProductMultiPositionFields.QUANTITY), 0));
                quantityComponent.requestComponentUpdateState();
                FieldComponent additionalQuantityComponent = formComponent.findFieldComponentByName("additionalQuantity");
                additionalQuantityComponent.setFieldValue(numberService.formatWithMinimumFractionDigits(
                        formEntity.getField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY), 0));
                additionalQuantityComponent.requestComponentUpdateState();
            }

        }

    }

    public void productChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity delivery = extractDeliveryEntityFromView(view);
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference("deliveredProductMultiPositions");
        List<FormComponent> formComponenets = deliveredProductMultiPositions.getFormComponents();
        for (FormComponent formComponent : formComponenets) {
            Entity formEntity = formComponent.getEntity();
            Entity product = formEntity.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);
            LookupComponent additionalCodeComponent = (LookupComponent) formComponent.findFieldComponentByName("additionalCode");
            LookupComponent productComponent = (LookupComponent) formComponent.findFieldComponentByName("product");

            if (productComponent.getUuid().equals(state.getUuid())) {
                formEntity.setField(DeliveredProductMultiPositionFields.ADDITIONAL_CODE, null);
                recalculateQuantities(delivery, formEntity);

                deliveredProductAddMultiHooks.boldRequired(formComponent);
                deliveredProductAddMultiHooks.filterAdditionalCode(product, additionalCodeComponent);
                if (product != null) {
                    String unit = product.getStringField(ProductFields.UNIT);
                    formEntity.setField(DeliveredProductMultiPositionFields.UNIT, unit);
                    String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
                    FieldComponent conversionComponent = formComponent.findFieldComponentByName("conversion");
                    if (additionalUnit != null) {
                        conversionComponent.setEnabled(true);
                        formEntity.setField(DeliveredProductMultiPositionFields.ADDITIONAL_UNIT, additionalUnit);
                        BigDecimal conversion = getConversion(product, unit, additionalUnit);
                        formEntity.setField(DeliveredProductMultiPositionFields.CONVERSION, conversion);
                    } else {
                        conversionComponent.setEnabled(false);
                        formEntity.setField(DeliveredProductMultiPositionFields.CONVERSION, BigDecimal.ONE);
                        formEntity.setField(DeliveredProductMultiPositionFields.ADDITIONAL_UNIT, unit);
                    }
                    formComponent.setEntity(formEntity);
                }
            }
        }
    }

    private Entity extractDeliveryEntityFromView(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity deliveredProductMulti = form.getPersistedEntityWithIncludedFormValues();
        return deliveredProductMulti.getBelongsToField(DeliveredProductMultiFields.DELIVERY);
    }

    private void recalculateQuantities(Entity delivery, Entity formEntity) {
        Entity product = formEntity.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);
        Entity additionalCode = formEntity.getBelongsToField(DeliveredProductMultiPositionFields.ADDITIONAL_CODE);
        BigDecimal conversion = BigDecimal.ONE;
        if (product != null) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            if (StringUtils.isNotEmpty(additionalUnit)) {
                conversion = formEntity.getDecimalField(DeliveredProductMultiPositionFields.CONVERSION);
                if (conversion == null) {
                    String unit = product.getStringField(ProductFields.UNIT);
                    conversion = getConversion(product, unit, additionalUnit);
                }
            }

            BigDecimal orderedQuantity = deliveredProductMultiPositionService.findOrderedQuantity(delivery, product,
                    additionalCode);
            BigDecimal alreadyAssignedQuantity = deliveredProductMultiPositionService.countAlreadyAssignedQuantityForProduct(
                    product, additionalCode, delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS));

            BigDecimal quantity = orderedQuantity.subtract(alreadyAssignedQuantity, numberService.getMathContext());
            if (BigDecimal.ZERO.compareTo(quantity) == 1) {
                quantity = BigDecimal.ZERO;
            }
            BigDecimal newAdditionalQuantity = quantity.multiply(conversion, numberService.getMathContext());
            newAdditionalQuantity = newAdditionalQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                    RoundingMode.HALF_UP);
            formEntity.setField(DeliveredProductMultiPositionFields.QUANTITY, quantity);
            formEntity.setField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY, newAdditionalQuantity);
        }
    }

    private BigDecimal getConversion(Entity product, String unit, String additionalUnit) {
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder
                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));
        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public void quantityChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference("deliveredProductMultiPositions");
        List<FormComponent> formComponenets = deliveredProductMultiPositions.getFormComponents();
        for (FormComponent formComponent : formComponenets) {
            Entity formEntity = formComponent.getPersistedEntityWithIncludedFormValues();
            BigDecimal quantity = formEntity.getDecimalField(DeliveredProductMultiPositionFields.QUANTITY);
            BigDecimal conversion = formEntity.getDecimalField(DeliveredProductMultiPositionFields.CONVERSION);
            if (conversion != null && quantity != null) {
                FieldComponent additionalQuantity = (FieldComponent) formComponent.findFieldComponentByName("additionalQuantity");
                BigDecimal newAdditionalQuantity = quantity.multiply(conversion, numberService.getMathContext());
                newAdditionalQuantity = newAdditionalQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                        RoundingMode.HALF_UP);
                additionalQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
                additionalQuantity.requestComponentUpdateState();
            }
        }
    }

    public void additionalQuantityChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference("deliveredProductMultiPositions");
        List<FormComponent> formComponenets = deliveredProductMultiPositions.getFormComponents();
        for (FormComponent formComponent : formComponenets) {
            Entity formEntity = formComponent.getPersistedEntityWithIncludedFormValues();
            BigDecimal additionalQuantity = formEntity.getDecimalField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY);
            BigDecimal conversion = formEntity.getDecimalField(DeliveredProductMultiPositionFields.CONVERSION);
            if (conversion != null && additionalQuantity != null) {
                FieldComponent quantity = (FieldComponent) formComponent.findFieldComponentByName("quantity");
                BigDecimal newQuantity = additionalQuantity.divide(conversion, numberService.getMathContext());
                newQuantity = newQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL, RoundingMode.HALF_UP);
                quantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newQuantity, 0));
                quantity.requestComponentUpdateState();
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
}
