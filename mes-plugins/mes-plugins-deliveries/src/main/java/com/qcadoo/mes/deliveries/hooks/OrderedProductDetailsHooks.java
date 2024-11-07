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
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.criteriaModifiers.QualityCardCriteriaModifiersD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class OrderedProductDetailsHooks {

    public static final String TOTAL_PRICE_CURRENCY = "totalPriceCurrency";
    public static final String PRICE_PER_UNIT_CURRENCY = "pricePerUnitCurrency";

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private BatchCriteriaModifier batchCriteriaModifier;

    @Autowired
    private SecurityService securityService;

    public void beforeRender(final ViewDefinitionState view) {

        FieldComponent batchNumber = (FieldComponent) view.getComponentByReference(OrderedProductFields.BATCH_NUMBER);
        if (((CheckBoxComponent) view.getComponentByReference(OrderedProductFields.ADD_BATCH)).isChecked()) {
            batchNumber.setEnabled(true);
        } else {
            batchNumber.setEnabled(false);
        }


        FormComponent orderedProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity orderedProduct = orderedProductForm.getPersistedEntityWithIncludedFormValues();

        fillUnitFields(view);
        fillCurrencyFields(view);
        togglePriceFields(view);

        setBatchLookupProductFilterValue(view, orderedProduct);
        fillCriteriaModifiers(view);
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList("orderedQuantityUnit");
        List<String> additionalUnitNames = Lists.newArrayList("additionalQuantityUnit");

        deliveriesService.fillUnitFields(view, OrderedProductFields.PRODUCT, referenceNames, additionalUnitNames);

        fillConversion(view);
    }

    private void fillConversion(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderedProductFields.PRODUCT);

        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

            FieldComponent conversionField = (FieldComponent) view.getComponentByReference(OrderedProductFields.CONVERSION);

            if (StringUtils.isEmpty(additionalUnit)) {
                conversionField.setFieldValue(BigDecimal.ONE);
                conversionField.setEnabled(false);
                conversionField.requestComponentUpdateState();
            }
        }
    }

    public void fillCurrencyFields(final ViewDefinitionState view) {
        FormComponent orderedProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        List<String> referenceNames = Lists.newArrayList(TOTAL_PRICE_CURRENCY, PRICE_PER_UNIT_CURRENCY);

        Entity orderedProduct = orderedProductForm.getEntity();
        Entity delivery = orderedProduct.getBelongsToField(OrderedProductFields.DELIVERY);

        deliveriesService.fillCurrencyFieldsForDelivery(view, referenceNames, delivery);
    }

    private void togglePriceFields(final ViewDefinitionState view) {
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(OrderedProductFields.PRICE_PER_UNIT);
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(OrderedProductFields.TOTAL_PRICE);
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

    public void setBatchLookupProductFilterValue(final ViewDefinitionState view, final Entity orderedProduct) {
        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(OrderedProductFields.BATCH);

        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);

        if (Objects.nonNull(product)) {
            batchCriteriaModifier.putProductFilterValue(batchLookup, product);
        }
    }

    private void fillCriteriaModifiers(final ViewDefinitionState viewDefinitionState) {
        LookupComponent product = (LookupComponent) viewDefinitionState.getComponentByReference("product");
        LookupComponent qualityCard = (LookupComponent) viewDefinitionState.getComponentByReference("qualityCard");
        if (product.getEntity() != null) {
            FilterValueHolder filter = qualityCard.getFilterValue();
            filter.put(QualityCardCriteriaModifiersD.L_PRODUCT_ID, product.getEntity().getId());
            qualityCard.setFilterValue(filter);
            qualityCard.requestComponentUpdateState();
        }
    }

}
