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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OperationProductInComponentViewHooks {

    private static final String L_PRODUCT_BATCH_REQUIRED = "productBatchRequired";

    private static final String L_BATCH_NUMBER_REQUIRED_PRODUCTS = "batchNumberRequiredProducts";

    @Autowired
    private ParameterService parameterService;

    public final void setOperationProductInComponentDefaultValue(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        setOperationProductInComponentDefaultValue(view);
    }

    public final void setOperationProductInComponentDefaultValue(final ViewDefinitionState view) {
        FormComponent form = getForm(view);

        FieldComponent productBatchRequired = getFieldComponent(view, L_PRODUCT_BATCH_REQUIRED);
        if (form.getEntityId() == null) {
            productBatchRequired.setFieldValue(isBatchNumberRequiredProducts());
        }
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference("form");
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

    public boolean isBatchNumberRequiredProducts() {
        Entity parameter = parameterService.getParameter();
        return parameter.getBooleanField(L_BATCH_NUMBER_REQUIRED_PRODUCTS);
    }

    public final void setDefaultProductBatchRequired(final ViewDefinitionState view) {
        FormComponent form = getForm(view);
        Entity parameter = parameterService.getParameter();
        boolean defaultProductBatchRequired = parameter.getBooleanField("batchNumberRequiredInputProducts");

        FieldComponent productBatchRequired = (FieldComponent) view.getComponentByReference("productBatchRequired");
        if (form.getEntityId() == null) {
            productBatchRequired.setFieldValue(defaultProductBatchRequired);
            productBatchRequired.requestComponentUpdateState();
        }

    }
}
