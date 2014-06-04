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
package com.qcadoo.mes.costNormsForMaterials.listeners;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyDetailsListenersCNFM {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    public final void showInputProductsCostInTechnology(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Long technologyId = (Long) componentState.getFieldValue();

        if (technologyId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", technologyId);

        String url = "../page/costNormsForMaterials/costNormsForMaterialsInTechnologyList.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    public void checkTechnologyProductsInNorms(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (technologyId == null) {
            return;
        }

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyId);

        List<Entity> technologyOperationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGY, technology)).list().getEntities();

        List<Entity> operationProductInComponents = Lists.newArrayList();

        for (Entity technologuOperationComponent : technologyOperationComponents) {
            operationProductInComponents.addAll(technologuOperationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS));
        }

        List<Entity> products = Lists.newArrayList();

        for (Entity operationProductInComponent : operationProductInComponents) {
            products.add(operationProductInComponent.getBelongsToField(BasicConstants.MODEL_PRODUCT));
        }

        for (Entity product : products) {
            if (technologyService.getProductType(product, technology).equals(TechnologyService.L_01_COMPONENT)
                    && ((product.getField(ProductFieldsCNFP.COST_FOR_NUMBER) == null)
                            || (product.getField(ProductFieldsCNFP.NOMINAL_COST) == null)
                            || (product.getField(ProductFieldsCNFP.LAST_PURCHASE_COST) == null) || (product
                            .getField(ProductFieldsCNFP.AVERAGE_COST) == null))) {
                technologyForm.addMessage("technologies.technologyDetails.error.inputProductsWithoutCostNorms", MessageType.INFO,
                        false);

                break;
            }
        }
    }

}
