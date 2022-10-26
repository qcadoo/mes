/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.OrderedProductConfiguratorFields;
import com.qcadoo.mes.masterOrders.criteriaModifier.OrderedProductConfiguratorCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderedProductConfiguratorDetailsHooks {

    private static final String L_PRODUCT_LOOKUP = "productLookup";

    public void onBeforeRender(final ViewDefinitionState view) {
        setCriteriaModifierParameters(view);
    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent orderedProductConfiguratorForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(L_PRODUCT_LOOKUP);

        Entity orderedProductConfigurator = orderedProductConfiguratorForm.getPersistedEntityWithIncludedFormValues();

        List<Entity> products = orderedProductConfigurator.getHasManyField(OrderedProductConfiguratorFields.PRODUCTS);

        List<Long> productIds = getProductIds(products);

        FilterValueHolder filterValueHolder = productLookup.getFilterValue();

        if (productIds.isEmpty()) {
            filterValueHolder.remove(OrderedProductConfiguratorCriteriaModifiers.PRODUCT_IDS);
        } else {
            filterValueHolder.put(OrderedProductConfiguratorCriteriaModifiers.PRODUCT_IDS, productIds);
        }

        productLookup.setFilterValue(filterValueHolder);
    }

    private List<Long> getProductIds(final List<Entity> products) {
        return products.stream().map(Entity::getId).collect(Collectors.toList());
    }

}
