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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
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
public class ProductsWithPriceHooksMFR {

    public static final String L_KEY_SELECTED_POSITIONS = "window.mainTab.form.selectedPositions";

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        JSONObject context = view.getJsonContext();
        String selectedProducts = context.getString(L_KEY_SELECTED_POSITIONS);
        List<Long> ids = Stream.of(selectedProducts.split(",")).map(Long::parseLong).collect(Collectors.toList());

        GridComponent positionsGrid = (GridComponent) view.getComponentByReference(DocumentFields.POSITIONS);

        final CustomRestriction selectedPositionsRestriction = scb -> scb.add(SearchRestrictions.in("id", ids));

        positionsGrid.setCustomRestriction(selectedPositionsRestriction);

    }
}
