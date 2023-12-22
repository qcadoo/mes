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
package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants.CURRENCY_FIELDS_ORDER;

import com.google.common.collect.ImmutableList;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.plugin.api.PluginUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.costNormsForProduct.hooks.ProductDetailsHooksCNFP;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TechnologyInstOperProductInCompDetailsHooks {

    private static final String PRODUCT_NUMBER = "productNumber";

    private static final String ORDER_ID = "orderId";

    private static final String L_COST_FOR_NUMBER_UNIT = "costForNumberUnit";
    public static final String L_SUB_ORDER = "subOrder";
    public static final String SUB_ORDER_IDS = "subOrderIds";

    @Autowired
    private ProductDetailsHooksCNFP productDetailsHooksCNFP;

    @Autowired
    private NumberService numberService;

    public void onBeforeRender(final ViewDefinitionState viewDefinitionState) {
        fillUnitField(viewDefinitionState);
        fillCurrencyFields(viewDefinitionState);
        setCriteriaModifiersParameters(viewDefinitionState);
        if(PluginUtils.isEnabled("supplyNegotiations")) {
            for (String reference : Arrays.asList("lastOfferCost", "averageOfferCost")) {
                FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(reference);
                field.setEnabled(false);
                field.requestComponentUpdateState();
            }
        }

    }

        public void fillUnitField(final ViewDefinitionState viewDefinitionState) {
        FieldComponent costForNumberUnit = (FieldComponent) viewDefinitionState.getComponentByReference(L_COST_FOR_NUMBER_UNIT);
        LookupComponent productLookup = (LookupComponent) viewDefinitionState
                .getComponentByReference(TechnologyInstOperProductInCompFields.PRODUCT);
        Entity product = productLookup.getEntity();
        if (product == null) {
            return;
        }
        String unit = product.getStringField(ProductFields.UNIT);
        costForNumberUnit.setFieldValue(unit);
        costForNumberUnit.requestComponentUpdateState();
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        productDetailsHooksCNFP.fillCurrencyFields(viewDefinitionState, CURRENCY_FIELDS_ORDER);
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity technologyInstOperProductInComp = form.getEntity();
        Entity product = technologyInstOperProductInComp.getBelongsToField(TechnologyInstOperProductInCompFields.PRODUCT);
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.COST_FOR_NUMBER),
                numberService.formatWithMinimumFractionDigits(product.getDecimalField(ProductFieldsCNFP.COST_FOR_NUMBER), 0));
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.NOMINAL_COST),
                numberService.formatWithMinimumFractionDigits(product.getDecimalField(ProductFieldsCNFP.NOMINAL_COST), 0));
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.NOMINAL_COST_CURRENCY),
                product.getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY) != null ? product
                        .getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY).getStringField(CurrencyFields.ALPHABETIC_CODE)
                        : "");
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.LAST_PURCHASE_COST),
                numberService.formatWithMinimumFractionDigits(product.getDecimalField(ProductFieldsCNFP.LAST_PURCHASE_COST), 0));
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.LAST_PURCHASE_COST_CURRENCY),
                product.getBelongsToField(ProductFieldsCNFP.LAST_PURCHASE_COST_CURRENCY) != null
                        ? product.getBelongsToField(ProductFieldsCNFP.LAST_PURCHASE_COST_CURRENCY).getStringField(
                                CurrencyFields.ALPHABETIC_CODE)
                        : "");
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.AVERAGE_COST),
                numberService.formatWithMinimumFractionDigits(product.getDecimalField(ProductFieldsCNFP.AVERAGE_COST), 0));
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.AVERAGE_COST_CURRENCY),
                product.getBelongsToField(ProductFieldsCNFP.AVERAGE_COST_CURRENCY) != null ? product
                        .getBelongsToField(ProductFieldsCNFP.AVERAGE_COST_CURRENCY).getStringField(CurrencyFields.ALPHABETIC_CODE)
                        : "");
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.LAST_OFFER_COST),
                numberService.formatWithMinimumFractionDigits(product.getDecimalField(ProductFieldsCNFP.LAST_OFFER_COST), 0));
        productDetailsHooksCNFP.fillField(
                (FieldComponent) viewDefinitionState.getComponentByReference(ProductFieldsCNFP.AVERAGE_OFFER_COST),
                numberService.formatWithMinimumFractionDigits(product.getDecimalField(ProductFieldsCNFP.AVERAGE_OFFER_COST), 0));
    }

    public void setCriteriaModifiersParameters(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent positions = (GridComponent) view.getComponentByReference("positions");
        FilterValueHolder filterValueHolder = positions.getFilterValue();

        Entity technologyInstOperProductInComp = form.getEntity();
        Entity product = technologyInstOperProductInComp.getBelongsToField(TechnologyInstOperProductInCompFields.PRODUCT);
        Entity order = technologyInstOperProductInComp.getBelongsToField(TechnologyInstOperProductInCompFields.ORDER);

        List<Entity> operationalTasks = order.getHasManyField(OrderFields.OPERATIONAL_TASKS);
        if(!operationalTasks.isEmpty()) {
            List<Long> subOrders = operationalTasks.stream()
                    .filter(ot -> Objects.nonNull(ot.getBelongsToField(L_SUB_ORDER)))
                    .map(ot -> ot.getBelongsToField(L_SUB_ORDER))
                    .map(so -> so.getId())
                    .collect(Collectors.toList());
            filterValueHolder.put(SUB_ORDER_IDS, ImmutableList.copyOf(subOrders));
        }

        filterValueHolder.put(PRODUCT_NUMBER, product.getStringField(ProductFields.NUMBER));
        filterValueHolder.put(ORDER_ID, order.getId().intValue());
        positions.setFilterValue(filterValueHolder);
    }
}
