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
package com.qcadoo.mes.techSubcontracting.aop;

import static com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields.OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields.PRODUCT;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.CHILDREN;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.techSubcontracting.constants.TechSubcontractingConstants;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginStateResolver;

@Service
public class ProductQuantitiesServiceImplTSOverrideUtil {

    @Autowired
    private PluginStateResolver pluginStateResolver;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public boolean shouldOverride() {
        return pluginStateResolver.isEnabled(TechSubcontractingConstants.PLUGIN_IDENTIFIER);
    }

    public Map<Long, BigDecimal> getProductComponentWithQuantitiesWithoutNonComponents(
            final Map<Long, BigDecimal> productComponentWithQuantities, final Set<Long> nonComponents) {
        for (Long nonComponent : nonComponents) {
            Entity operationProductComponent = productQuantitiesService.getOperationProductComponent(nonComponent);

            Entity product = operationProductComponent.getBelongsToField(PRODUCT);
            Entity technologyOperationComponent = operationProductComponent.getBelongsToField(OPERATION_COMPONENT);

            List<Entity> children = technologyOperationComponent.getHasManyField(CHILDREN).find()
                    .add(SearchRestrictions.eq("isSubcontracting", true)).list().getEntities();

            boolean isSubcontracting = false;
            for (Entity child : children) {
                Entity operationProductOutComponent = child.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS).find()
                        .add(SearchRestrictions.belongsTo(PRODUCT, product)).setMaxResults(1).uniqueResult();

                if (operationProductOutComponent != null) {
                    isSubcontracting = true;
                }
            }

            if (!isSubcontracting) {
                productComponentWithQuantities.remove(nonComponent);
            }
        }

        return productComponentWithQuantities;
    }

}
