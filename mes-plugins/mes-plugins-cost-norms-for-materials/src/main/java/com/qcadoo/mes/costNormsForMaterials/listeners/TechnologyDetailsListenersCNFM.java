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
package com.qcadoo.mes.costNormsForMaterials.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<Entity> products = findComponentsForTechnology(technologyId);

        for (Entity product : products) {
            if ((product.getField(ProductFieldsCNFP.COST_FOR_NUMBER) == null)
                            || (product.getField(ProductFieldsCNFP.NOMINAL_COST) == null)
                            || (product.getField(ProductFieldsCNFP.LAST_PURCHASE_COST) == null) || (product
                            .getField(ProductFieldsCNFP.AVERAGE_COST) == null)) {
                technologyForm.addMessage("technologies.technologyDetails.error.inputProductsWithoutCostNorms", MessageType.INFO,
                        false);
                break;
            }
        }
    }

    private List<Entity> findComponentsForTechnology(final Long technologyId) {
         String query = "select DISTINCT opic.id as opicId, "
                + "opic.product as product, "
                + "(select count(*) from "
                + "#technologies_operationProductOutComponent opoc "
                + "left join opoc.operationComponent oc  "
                + "left join oc.technology as tech "
                + "left join oc.parent par  "
                + "where "
                + "opoc.product = inputProd and par.id = toc.id ) as isIntermediate "
                + "from #technologies_operationProductInComponent opic "
                + "left join opic.product as inputProd "
                + "left join opic.operationComponent toc "
                + "left join toc.technology tech "
                + "where tech.id = :technologyId ";
        List<Entity> allProducts = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)
                .find(query)
                .setLong("technologyId", technologyId).list().getEntities();

        List<Entity> components = allProducts.stream().filter(p -> (Long) p.getField("isIntermediate") == 0l).map(cmp -> cmp.getBelongsToField("product"))
                .collect(Collectors.toList());
        return components;
    }

}
