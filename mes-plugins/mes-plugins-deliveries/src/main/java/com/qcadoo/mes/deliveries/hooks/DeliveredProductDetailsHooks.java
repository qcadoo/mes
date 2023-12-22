/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deliveries.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogy.criteriaModifier.BatchCriteriaModifier;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DeliveredProductDetailsHooks {

    private static final String L_LOCATION = "location";

    private static final String TOTAL_PRICE_CURRENCY = "totalPriceCurrency";

    private static final String PRICE_PER_UNIT_CURRENCY = "pricePerUnitCurrency";

    @Autowired
    private NumberService numberService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private BatchCriteriaModifier batchCriteriaModifier;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent deliveredProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent batchNumber = (FieldComponent) view.getComponentByReference(DeliveredProductFields.BATCH_NUMBER);
        CheckBoxComponent addBatchCheckBox = (CheckBoxComponent) view.getComponentByReference(DeliveredProductFields.ADD_BATCH);

        Entity deliveredProduct = deliveredProductForm.getPersistedEntityWithIncludedFormValues();

        batchNumber.setEnabled(addBatchCheckBox.isChecked());

        fillOrderedQuantities(view);
        fillUnitFields(view);
        fillCurrencyFields(view);
        togglePriceFields(view);
        setDeliveredQuantityFieldRequired(view);
        setAdditionalQuantityFieldRequired(view);
        lockConversion(view);
        setFilters(view);

        setBatchLookupProductFilterValue(view, deliveredProduct);
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        List<String> unitNames = Lists.newArrayList("damagedQuantityUnit", "deliveredQuantityUnit", "orderedQuantityUnit");
        List<String> additionalUnitNames = Lists.newArrayList("additionalQuantityUnit");

        deliveriesService.fillUnitFields(view, DeliveredProductFields.PRODUCT, unitNames, additionalUnitNames);
    }

    public void fillCurrencyFields(final ViewDefinitionState view) {
        FormComponent deliveredProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        List<String> referenceNames = Lists.newArrayList(TOTAL_PRICE_CURRENCY, PRICE_PER_UNIT_CURRENCY);

        Entity deliveredProduct = deliveredProductForm.getEntity();
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);

        deliveriesService.fillCurrencyFieldsForDelivery(view, referenceNames, delivery);
    }

    private void togglePriceFields(final ViewDefinitionState view) {
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(DeliveredProductFields.PRICE_PER_UNIT);
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(DeliveredProductFields.TOTAL_PRICE);
        FieldComponent pricePerUnitCurrencyField = (FieldComponent) view.getComponentByReference(PRICE_PER_UNIT_CURRENCY);
        FieldComponent totalPriceCurrencyField = (FieldComponent) view.getComponentByReference(TOTAL_PRICE_CURRENCY);
        ComponentState priceBorderLayout = view.getComponentByReference("priceBorderLayout");

        boolean hasCurrentUserRole = securityService.hasCurrentUserRole("ROLE_DELIVERIES_PRICE");

        pricePerUnitField.setVisible(hasCurrentUserRole);
        totalPriceField.setVisible(hasCurrentUserRole);
        pricePerUnitCurrencyField.setVisible(hasCurrentUserRole);
        totalPriceCurrencyField.setVisible(hasCurrentUserRole);
        priceBorderLayout.setVisible(hasCurrentUserRole);
    }

    public void fillPalletTypeField(final ViewDefinitionState view) {
        FormComponent deliveredProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PALLET_NUMBER);
        FieldComponent palletTypeField = (FieldComponent) view.getComponentByReference(DeliveredProductFields.PALLET_TYPE);

        Entity deliveredProduct = deliveredProductForm.getEntity();
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);

        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);
        Entity palletNumber = palletNumberLookup.getEntity();
        String palletType = null;

        if (Objects.nonNull(palletNumber)) {
            palletType = materialFlowResourcesService.getTypeOfPalletByPalletNumber(location.getId(), palletNumber.getStringField(PalletNumberFields.NUMBER));
        }

        palletTypeField.setFieldValue(palletType);
        palletTypeField.requestComponentUpdateState();
    }

    public void fillOrderedQuantities(final ViewDefinitionState view) {
        FormComponent deliveredProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PRODUCT);
        FieldComponent orderedQuantity = (FieldComponent) view.getComponentByReference(OrderedProductFields.ORDERED_QUANTITY);

        Entity deliveredProduct = deliveredProductForm.getEntity();
        Entity product = productLookup.getEntity();

        if (Objects.isNull(product)) {
            orderedQuantity.setFieldValue(null);
        } else {
            orderedQuantity.setFieldValue(numberService.format(getOrderedProductQuantity(deliveredProduct)));
        }

        orderedQuantity.requestComponentUpdateState();
    }

    public void fillConversion(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PRODUCT);

        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            String unit = product.getStringField(ProductFields.UNIT);

            FieldComponent conversionField = (FieldComponent) view.getComponentByReference(DeliveredProductFields.CONVERSION);

            if (StringUtils.isEmpty(additionalUnit)) {
                conversionField.setFieldValue(BigDecimal.ONE);
                conversionField.setEnabled(false);
            } else {
                String conversion = numberService.formatWithMinimumFractionDigits(getConversion(product, unit, additionalUnit),
                        0);

                conversionField.setFieldValue(conversion);
                conversionField.setEnabled(true);
            }

            conversionField.requestComponentUpdateState();
        }
    }

    public BigDecimal getDefaultConversion(final Entity product) {
        if (Objects.nonNull(product)) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            String unit = product.getStringField(ProductFields.UNIT);

            if (StringUtils.isNotEmpty(additionalUnit) && !unit.equals(additionalUnit)) {
                return getConversion(product, unit, additionalUnit);
            }
        }

        return BigDecimal.ONE;
    }

    private BigDecimal getConversion(final Entity product, final String unit, final String additionalUnit) {
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder
                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getOrderedProductQuantity(final Entity deliveredProduct) {
        BigDecimal orderedQuantity = null;

        Optional<Entity> maybeOrderedProduct = deliveriesService.getOrderedProductForDeliveredProduct(deliveredProduct);

        if (maybeOrderedProduct.isPresent()) {
            Entity orderedProduct = maybeOrderedProduct.get();

            orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        } else {
            maybeOrderedProduct = deliveriesService.getSuitableOrderedProductForDeliveredProduct(deliveredProduct);

            if (maybeOrderedProduct.isPresent()) {
                Entity orderedProduct = maybeOrderedProduct.get();

                orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
            }
        }

        return orderedQuantity;
    }

    public void setDeliveredQuantityFieldRequired(final ViewDefinitionState view) {
        FieldComponent deliveredQuantityField = (FieldComponent) view
                .getComponentByReference(DeliveredProductFields.DELIVERED_QUANTITY);

        deliveredQuantityField.setRequired(true);
        deliveredQuantityField.requestComponentUpdateState();
    }

    public void setAdditionalQuantityFieldRequired(final ViewDefinitionState view) {
        FieldComponent additionalQuantityField = (FieldComponent) view
                .getComponentByReference(DeliveredProductFields.ADDITIONAL_QUANTITY);

        additionalQuantityField.setRequired(true);
        additionalQuantityField.requestComponentUpdateState();
    }

    private void lockConversion(final ViewDefinitionState view) {
        String unit = (String) view.getComponentByReference("deliveredQuantityUnit").getFieldValue();
        String additionalUnit = (String) view.getComponentByReference("additionalQuantityUnit").getFieldValue();

        if (Objects.nonNull(additionalUnit) && additionalUnit.equals(unit)) {
            view.getComponentByReference(DeliveredProductFields.CONVERSION).setEnabled(false);
        }
    }

    private void setFilters(final ViewDefinitionState view) {
        FormComponent deliveredProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PRODUCT);
        LookupComponent storageLocationsLookup = (LookupComponent) view
                .getComponentByReference(DeliveredProductFields.STORAGE_LOCATION);

        Entity deliveredProductEntity = deliveredProductForm.getEntity();
        Entity product = productLookup.getEntity();

        Entity delivery = deliveredProductEntity.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        if (Objects.nonNull(product) && Objects.nonNull(location)) {
            filterBy(storageLocationsLookup, L_LOCATION, location.getId());
        } else {
            storageLocationsLookup.setFieldValue(null);
            storageLocationsLookup.setEnabled(false);
            storageLocationsLookup.requestComponentUpdateState();
        }
    }

    private void filterBy(final LookupComponent componentState, final String field, final Long id) {
        FilterValueHolder filterValueHolder = componentState.getFilterValue();
        filterValueHolder.put(field, id);

        componentState.setEnabled(true);
        componentState.setFilterValue(filterValueHolder);
        componentState.requestComponentUpdateState();
    }

    public void setBatchLookupProductFilterValue(final ViewDefinitionState view, final Entity deliveredProduct) {
        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.BATCH);

        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        if (Objects.nonNull(product)) {
            batchCriteriaModifier.putProductFilterValue(batchLookup, product);
        }
    }

}
