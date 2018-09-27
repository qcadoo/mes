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
package com.qcadoo.mes.advancedGenealogyForOrders.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TrackingForOrdersListeners {

    private static final String L_GENEALOGY_PRODUCT_IN_COMPONENTS_LIST = "genealogyProductInComponentsList";

    public void onAddRow(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view
                .getComponentByReference(L_GENEALOGY_PRODUCT_IN_COMPONENTS_LIST);

        for (FormComponent innerForm : adl.getFormComponents()) {
            AwesomeDynamicListComponent innerAdl = (AwesomeDynamicListComponent) innerForm
                    .findFieldComponentByName("productInBatches");
            Entity productInComponent = innerForm.getPersistedEntityWithIncludedFormValues().getBelongsToField(
                    "productInComponent");
            Entity innerProduct = productInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
            for (FormComponent batchForm : innerAdl.getFormComponents()) {
                if (batchForm.getEntityId() == null) {
                    LookupComponent batchLookup = (LookupComponent) batchForm.findFieldComponentByName("inputProductBatchLookup");
                    FilterValueHolder innerFilter = batchLookup.getFilterValue();
                    innerFilter.put("productForBatch", innerProduct.getId());
                    batchLookup.setFilterValue(innerFilter);
                }
            }

        }

    }
}
