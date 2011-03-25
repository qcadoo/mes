/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.products.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.components.FieldComponentState;
import com.qcadoo.view.components.lookup.LookupComponentState;

@Service
public class UnitService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillProductUnit(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        fillProductUnitPreRender(viewDefinitionState, viewDefinitionState.getLocale());
    }

    public void fillProductUnitPreRender(final ViewDefinitionState state, final Locale locale) {
        LookupComponentState productState = (LookupComponentState) state.getComponentByReference("product");
        FieldComponentState unitState = (FieldComponentState) state.getComponentByReference("unit");
        unitState.requestComponentUpdateState();
        if (productState.getFieldValue() != null) {
            Entity product = dataDefinitionService.get("products", "product").get(productState.getFieldValue());
            unitState.setFieldValue(product.getStringField("unit"));
        } else {
            unitState.setFieldValue("");
        }
    }
}
