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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiPositionFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.hooks.DeliveredProductAddMultiHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

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

    public void createDeliveriedProducts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        state.performEvent(view, "save");
        Entity deliveredProductMulti = form.getPersistedEntityWithIncludedFormValues();
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
        DataDefinition deliveredProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
        List<Entity> deliveredProducts = Lists.newArrayList(delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS));
        for (Entity position : deliveredProductMultiPositions) {
            Entity deliveredProduct = createDeliveredProduct(position, deliveredProductDD);
            setStorageLocationFields(deliveredProduct, deliveredProductMulti);
            deliveredProduct.setField(DeliveredProductFields.DELIVERY, delivery);
            deliveredProduct = deliveredProductDD.save(deliveredProduct);
            deliveredProducts.add(deliveredProduct);
        }
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS, deliveredProducts);
        delivery.getDataDefinition().save(delivery);
        view.addMessage("deliveries.deliveredProductMulti.success", MessageType.SUCCESS);
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
        for (Entity position : deliveredProductMultiPositions) {
            checkMissing(position, DeliveredProductMultiPositionFields.PRODUCT, positionDataDefinition);
            checkMissingOrZero(position, DeliveredProductMultiPositionFields.QUANTITY, positionDataDefinition);
            checkMissingOrZero(position, DeliveredProductMultiPositionFields.CONVERSION, positionDataDefinition);
            if (position.isValid()) {
                Long productId = position.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT).getId();
                Date expirationDate = position.getDateField(DeliveredProductMultiPositionFields.EXPIRATION_DATE);
                if (positionsMap.containsEntry(productId, expirationDate)) {
                    position.addError(positionDataDefinition.getField(DeliveredProductMultiPositionFields.PRODUCT),
                            "deliveries.deliveredProductMulti.error.productExists");
                } else {
                    positionsMap.put(productId, expirationDate);
                }
            }
            isValid = isValid && position.isValid();
        }
        return isValid;
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

    public void productChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent deliveredProductMultiPositions = (AwesomeDynamicListComponent) view
                .getComponentByReference("deliveredProductMultiPositions");
        List<FormComponent> formComponenets = deliveredProductMultiPositions.getFormComponents();
        for (FormComponent formComponent : formComponenets) {
            Entity formEntity = formComponent.getEntity();
            Entity product = formEntity.getBelongsToField(DeliveredProductMultiPositionFields.PRODUCT);
            LookupComponent additionalCodeComponent = (LookupComponent) formComponent.findFieldComponentByName("additionalCode");
            deliveredProductAddMultiHooks.boldRequired(formComponent);
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
                additionalCodeComponent.setEnabled(true);
                FilterValueHolder filterValueHolder = additionalCodeComponent.getFilterValue();
                filterValueHolder.put("product", product.getId());
                additionalCodeComponent.setFilterValue(filterValueHolder);
                additionalCodeComponent.requestComponentUpdateState();
            } else {
                additionalCodeComponent.setFieldValue(null);
                additionalCodeComponent.setEnabled(false);
                additionalCodeComponent.requestComponentUpdateState();
            }
        }
    }

    private BigDecimal getConversion(Entity product, String unit, String additionalUnit) {
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                        UnitConversionItemFieldsB.PRODUCT, product)));
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
