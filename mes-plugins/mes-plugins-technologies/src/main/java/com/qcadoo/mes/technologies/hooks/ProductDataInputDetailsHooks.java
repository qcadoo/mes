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
package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.ProductDataFields;
import com.qcadoo.mes.technologies.constants.ProductDataInputFields;
import com.qcadoo.mes.technologies.criteriaModifiers.ProductDataCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductDataInputDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        setCriteriaModifierParameters(view);
    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent productDataInputForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent operationProductInComponentDtosGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity productDataInput = productDataInputForm.getEntity();

        Entity productData = productDataInput.getBelongsToField(ProductDataInputFields.PRODUCT_DATA);
        Entity technology = productData.getBelongsToField(ProductDataFields.TECHNOLOGY);
        List<Entity> productDataInputs = productData.getHasManyField(ProductDataFields.PRODUCT_DATA_INPUTS);

        List<Long> operationProductDataInComponents = getOperationProductDataInComponents(productDataInputs);

        FilterValueHolder filterValueHolder = operationProductInComponentDtosGrid.getFilterValue();

        if (Objects.isNull(technology)) {
            filterValueHolder.remove(ProductDataCriteriaModifiers.TECHNOLOGY_ID);
        } else {
            filterValueHolder.put(ProductDataCriteriaModifiers.TECHNOLOGY_ID, technology.getId());
        }
        if (operationProductDataInComponents.isEmpty()) {
            filterValueHolder.remove(ProductDataCriteriaModifiers.OPERATION_PRODUCT_IN_COMPONENT_IDS);
        } else {
            filterValueHolder.put(ProductDataCriteriaModifiers.OPERATION_PRODUCT_IN_COMPONENT_IDS, operationProductDataInComponents);
        }

        operationProductInComponentDtosGrid.setFilterValue(filterValueHolder);
    }

    private List<Long> getOperationProductDataInComponents(final List<Entity> productDataInputs) {
        return productDataInputs.stream().map(productDataInput -> productDataInput.getBelongsToField(ProductDataInputFields.OPERATION_PRODUCT_IN_COMPONENT).getId()).collect(Collectors.toList());
    }

}
