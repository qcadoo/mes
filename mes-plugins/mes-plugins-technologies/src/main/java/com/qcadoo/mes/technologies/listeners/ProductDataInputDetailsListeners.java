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
package com.qcadoo.mes.technologies.listeners;

import com.qcadoo.mes.technologies.constants.ProductDataInputFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.services.ProductDataService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductDataInputDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductDataService productDataService;

    public void createProductDataInputs(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent productDataInputForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent operationProductInComponentDtosGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity productDataInput = productDataInputForm.getEntity();

        List<Entity> operationProductInComponentDtos = operationProductInComponentDtosGrid.getSelectedEntities();

        Entity productData = productDataInput.getBelongsToField(ProductDataInputFields.PRODUCT_DATA);
        List<Entity> operationProductInComponents = getOperationProductInComponents(operationProductInComponentDtos);

        productDataService.createProductDataInputs(productData, operationProductInComponents);
    }

    private List<Entity> getOperationProductInComponents(final List<Entity> operationProductInComponentDtos) {
        List<Long> ids = operationProductInComponentDtos.stream().map(Entity::getId).collect(Collectors.toList());

        return getOperationProductInComponentDD().find().add(SearchRestrictions.in("id", ids)).list().getEntities();
    }

    private DataDefinition getOperationProductInComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

}
