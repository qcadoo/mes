/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.MrpAlgorithmStrategy;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;

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
            if (operationProductComponentModelName.equals(productComponentWithQuantity.getKey().getDataDefinition().getName())) {
                if (nonComponents.contains(productComponentWithQuantity.getKey())) {
                    continue;
                }

                productQuantitiesService.addProductQuantitiesToList(productComponentWithQuantity, productWithQuantities);
            } else {
                Entity operation = productComponentWithQuantity.getKey().getBelongsToField("operationComponent");

                if (operation.getBooleanField("isSubcontracting")) {
                    productQuantitiesService.addProductQuantitiesToList(productComponentWithQuantity, productWithQuantities);
                }
            }
        }

        return productWithQuantities;
    }

}
