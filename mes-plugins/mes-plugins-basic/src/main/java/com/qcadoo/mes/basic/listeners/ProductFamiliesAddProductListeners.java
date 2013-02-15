/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.basic.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.hooks.ProductFamiliesAddProductHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductFamiliesAddProductListeners {

    @Autowired
    private ProductFamiliesAddProductHooks productFamiliesAddProductHooks;

    public final void addSelectedProductToFamily(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity formEntity = form.getEntity();
        LookupComponent childLookup = (LookupComponent) view.getComponentByReference("child");
        Entity child = childLookup.getEntity();
        if (child == null) {
            return;
        } else {
            child.setField(ProductFields.PARENT, formEntity.getField(ProductFields.PARENT));
            child.getDataDefinition().save(child);
        }
        childLookup.requestComponentUpdateState();
        form.setEntity(formEntity);
        view.performEvent(view, "refresh");
    }

    public void changeProductInLookup(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productFamiliesAddProductHooks.updateRibbonState(view);
    }

}
