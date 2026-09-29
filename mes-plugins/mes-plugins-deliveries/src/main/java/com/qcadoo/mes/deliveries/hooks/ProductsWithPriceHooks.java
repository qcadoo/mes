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

import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductsWithPriceHooks {

    public static final String L_KEY_SELECTED_PRODUCTS = "window.mainTab.form.selectedProducts";

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        JSONObject context = view.getJsonContext();
        String selectedProducts = context.getString(L_KEY_SELECTED_PRODUCTS);
        List<Long> ids = Stream.of(selectedProducts.split(",")).map(Long::parseLong).collect(Collectors.toList());

        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);

        final CustomRestriction selectedProductsRestriction = scb -> scb.add(SearchRestrictions.in("id", ids));

        deliveredProductsGrid.setCustomRestriction(selectedProductsRestriction);

    }
}
