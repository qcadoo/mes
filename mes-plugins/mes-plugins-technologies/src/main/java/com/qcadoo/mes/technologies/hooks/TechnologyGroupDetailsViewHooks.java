/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.technologies.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyGroupDetailsViewHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addTechnologyGroupToProduct(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent technologyGroupForm = (FormComponent) view.getComponentByReference("form");
        Entity technologyGroup = technologyGroupForm.getEntity();

        if (technologyGroup.getId() == null) {
            return;
        }

        FormComponent productForm = (FormComponent) view.getComponentByReference("product");
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        product = getProductFromDB(product.getId());

        product.setField("technologyGroup", technologyGroup);
        product.getDataDefinition().save(product);
    }

    private Entity getProductFromDB(final Long productId) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
    }
}
