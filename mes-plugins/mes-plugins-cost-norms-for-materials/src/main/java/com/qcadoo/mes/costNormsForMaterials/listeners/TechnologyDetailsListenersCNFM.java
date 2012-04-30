/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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

import static com.qcadoo.mes.costNormsForProduct.constants.ProductCostNormsFields.AVERAGE_COST;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductCostNormsFields.COST_FOR_NUMBER;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductCostNormsFields.LAST_PURCHASE_COST;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductCostNormsFields.NOMINAL_COST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;

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
        parameters.put("technology.id", technologyId);

        String url = "../page/costNormsForMaterials/costNormsForMaterialsInTechnologyList.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    public void checkTechnologyProductsInNorms(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        ComponentState form = viewDefinitionState.getComponentByReference(L_FORM);

        if (form.getFieldValue() == null) {
            return;
        }

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) form.getFieldValue());
        List<Entity> operationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGY, technology)).list().getEntities();
        List<Entity> productInComponents = new ArrayList<Entity>();
        for (Entity operationComponent : operationComponents) {
            productInComponents.addAll(operationComponent.getHasManyField(TechnologiesConstants.OPERATION_PRODUCT_IN_COMPONENTS));
        }
        List<Entity> products = new ArrayList<Entity>();
        for (Entity productInComponent : productInComponents) {
            products.add(productInComponent.getBelongsToField(BasicConstants.MODEL_PRODUCT));
        }
        for (Entity product : products) {
            if (technologyService.getProductType(product, technology).equals(TechnologyService.L_01_COMPONENT)
                    && (product.getField(COST_FOR_NUMBER) == null || product.getField(NOMINAL_COST) == null
                            || product.getField(LAST_PURCHASE_COST) == null || product.getField(AVERAGE_COST) == null)) {
                form.addMessage("technologies.technologyDetails.error.inputProductsWithoutCostNorms", MessageType.INFO, false);
                break;
            }
        }
    }
}
