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
package com.qcadoo.mes.techSubcontracting;

import static com.qcadoo.mes.technologies.constants.OperationProductInComponentFields.OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.OperationProductInComponentFields.PRODUCT;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.CHILDREN;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.MrpAlgorithmStrategy;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service("mrpAlgorithmStrategyTS")
public class MrpAlgorithmStrategyTS implements MrpAlgorithmStrategy {

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public boolean isApplicableFor(final MrpAlgorithm mrpAlgorithm) {
        return MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS.equals(mrpAlgorithm);
    }

    public Map<Entity, BigDecimal> perform(final Map<Entity, BigDecimal> productComponentWithQuantities,
            final Set<Entity> nonComponents, final MrpAlgorithm mrpAlgorithm, final String operationProductComponentModelName) {
        Map<Entity, BigDecimal> productWithQuantities = new HashMap<Entity, BigDecimal>();

        for (Entry<Entity, BigDecimal> productComponentWithQuantity : productComponentWithQuantities.entrySet()) {
            Entity operationProductComponent = productComponentWithQuantity.getKey();

            if (operationProductComponentModelName.equals(operationProductComponent.getDataDefinition().getName())) {
                if (nonComponents.contains(operationProductComponent)) {

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
                        continue;
                    }
                }

                productQuantitiesService.addProductQuantitiesToList(productComponentWithQuantity, productWithQuantities);
            }
        }

        return productWithQuantities;
    }

}
