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
package com.qcadoo.mes.deliveries.hooks;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class DeliveredProductDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private UnitConversionService unitConversionService;

    public void beforeRender(final ViewDefinitionState view) {
        fillOrderedQuantities(view);
        fillUnitFields(view);
        fillCurrencyFields(view);
        setDeliveredQuantityFieldRequired(view);
        setAdditionalQuantityFieldRequired(view);
        lockConversion(view);
        setFilters(view);
        disableReservationsForWaste((view));
    }

    private void disableReservationsForWaste(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity deliveredProduct = form.getEntity();
        GridComponent reservations = (GridComponent) view.getComponentByReference("deliveredProductReservations");
        boolean enabled = !deliveredProduct.getBooleanField(DeliveredProductFields.IS_WASTE);
        reservations.setEnabled(enabled);
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        List<String> unitNames = Lists.newArrayList("damagedQuantityUnit", "deliveredQuantityUnit", "orderedQuantityUnit");
        List<String> additionalUnitNames = Lists.newArrayList("additionalQuantityUnit");

        deliveriesService.fillUnitFields(view, DeliveredProductFields.PRODUCT, unitNames, additionalUnitNames);
    }

    public void fillCurrencyFields(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList("totalPriceCurrency", "pricePerUnitCurrency");

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity deliveredProduct = form.getEntity();
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);

        deliveriesService.fillCurrencyFieldsForDelivery(view, referenceNames, delivery);
    }

    public void fillOrderedQuantities(final ViewDefinitionState view) {
        FormComponent deliveredProductForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity deliveredProduct = deliveredProductForm.getEntity();

        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PRODUCT);
        Entity product = productLookup.getEntity();

        FieldComponent orderedQuantity = (FieldComponent) view.getComponentByReference(OrderedProductFields.ORDERED_QUANTITY);

        if (product == null) {
            orderedQuantity.setFieldValue(null);
        } else {
            orderedQuantity.setFieldValue(numberService.format(getOrderedProductQuantity(deliveredProduct)));
        }

        orderedQuantity.requestComponentUpdateState();
    }

    public void fillConversion(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PRODUCT);
        Entity product = productLookup.getEntity();

        if (product != null) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            String unit = product.getStringField(ProductFields.UNIT);
            FieldComponent conversionField = (FieldComponent) view.getComponentByReference("conversion");
            if (StringUtils.isEmpty(additionalUnit)) {
                conversionField.setFieldValue(BigDecimal.ONE);
                conversionField.setEnabled(false);
                conversionField.requestComponentUpdateState();
            } else {
                String conversion = numberService.formatWithMinimumFractionDigits(getConversion(product, unit, additionalUnit),
                        0);
                conversionField.setFieldValue(conversion);
                conversionField.setEnabled(true);
                conversionField.requestComponentUpdateState();
            }
        }
    }

    public BigDecimal getDefaultConversion(Entity product) {
        if (product != null) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            String unit = product.getStringField(ProductFields.UNIT);
            if (StringUtils.isNotEmpty(additionalUnit) && !unit.equals(additionalUnit)) {
                return getConversion(product, unit, additionalUnit);
            }
        }
        return BigDecimal.ONE;
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

    private BigDecimal getOrderedProductQuantity(final Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        BigDecimal orderedQuantity = null;

        Entity orderedProduct = deliveriesService.getOrderedProductDD().find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY, delivery))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product)).setMaxResults(1).uniqueResult();

        if (orderedProduct != null) {
            orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        }

        return orderedQuantity;
    }

    public void setDeliveredQuantityFieldRequired(final ViewDefinitionState view) {
        FieldComponent delivedQuantity = (FieldComponent) view.getComponentByReference(DeliveredProductFields.DELIVERED_QUANTITY);
        delivedQuantity.setRequired(true);
        delivedQuantity.requestComponentUpdateState();
    }

    public void setAdditionalQuantityFieldRequired(final ViewDefinitionState view) {
        FieldComponent delivedQuantity = (FieldComponent) view
                .getComponentByReference(DeliveredProductFields.ADDITIONAL_QUANTITY);
        delivedQuantity.setRequired(true);
        delivedQuantity.requestComponentUpdateState();
    }

    private void lockConversion(ViewDefinitionState view) {
        String unit = (String) view.getComponentByReference("deliveredQuantityUnit").getFieldValue();
        String additionalUnit = (String) view.getComponentByReference("additionalQuantityUnit").getFieldValue();
        if (additionalUnit != null && additionalUnit.equals(unit)) {
            view.getComponentByReference("conversion").setEnabled(false);
        }
    }

    private void setFilters(ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PRODUCT);
        Entity product = productLookup.getEntity();
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity deliveredProductEntity = form.getEntity();
        Entity delivery = deliveredProductEntity.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        LookupComponent storageLocationsLookup = (LookupComponent) view
                .getComponentByReference(DeliveredProductFields.STORAGE_LOCATION);
        LookupComponent additionalCodeLookup = (LookupComponent) view
                .getComponentByReference(DeliveredProductFields.ADDITIONAL_CODE);

        if (product != null) {
            filterBy(additionalCodeLookup, DeliveredProductFields.PRODUCT, product.getId());
        }

        if (product != null && location != null) {
            filterBy(storageLocationsLookup, "location", location.getId());
            filterBy(storageLocationsLookup, "product", product.getId());

        } else {
            storageLocationsLookup.setFieldValue(null);
            storageLocationsLookup.setEnabled(false);
            storageLocationsLookup.requestComponentUpdateState();
        }
    }

    private void filterBy(LookupComponent component, String field, Long id) {
        component.setEnabled(true);
        FilterValueHolder filterValueHolder = component.getFilterValue();
        filterValueHolder.put(field, id);
        component.setFilterValue(filterValueHolder);
        component.requestComponentUpdateState();
    }
}
