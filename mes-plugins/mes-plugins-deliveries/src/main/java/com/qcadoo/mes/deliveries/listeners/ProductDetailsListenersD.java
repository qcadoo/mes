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
package com.qcadoo.mes.deliveries.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.CompanyProductService;
import com.qcadoo.mes.deliveries.hooks.ProductDetailsHooksD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductDetailsListenersD {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CompanyProductService companyProductService;

    @Autowired
    private ProductDetailsHooksD productDetailsHooksD;

    public void checkIfDefaultSupplierIsUnique(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        DataDefinition productDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        Entity formProduct = form.getPersistedEntityWithIncludedFormValues();
        Entity product = formProduct.getId() != null ? productDD.get(formProduct.getId()) : formProduct;
        Entity formParent = formProduct.getBelongsToField(ProductFields.PARENT);
        if (formParent != null) {
            Entity parent = productDD.get(formParent.getId());
            if (companyProductService.checkIfDefaultExistsForProductFamily(parent)
                    && companyProductService.checkIfDefaultExistsForParticularProduct(product)) {
                formProduct.addError(productDD.getField(ProductFields.PARENT),
                        "basic.company.message.defaultAlreadyExistsForProductAndFamily");
                form.setEntity(formProduct);
            }
        }
    }

    public void toggleSuppliersGrids(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        productDetailsHooksD.toggleSuppliersGrids(view);
    }
}
