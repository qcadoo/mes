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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.MrpAlgorithmStrategy;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;

@Service("mrpAlgorithmStrategyTS")
public class MrpAlgorithmStrategyTS implements MrpAlgorithmStrategy {

    public boolean isApplicableFor(final MrpAlgorithm algorithm) {
        return MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS.equals(algorithm);
    }

    public Map<Entity, BigDecimal> perform(final Map<Entity, BigDecimal> productComponentQuantities,
            final Set<Entity> nonComponents, final MrpAlgorithm algorithm, final String type) {
        Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();

        for (Entry<Entity, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            if (type.equals(productComponentQuantity.getKey().getDataDefinition().getName())) {
                if (nonComponents.contains(productComponentQuantity.getKey())) {
                    continue;
                }
                addProductQuantitiesToList(productComponentQuantity, productQuantities);
            } else {
                Entity operation = productComponentQuantity.getKey().getBelongsToField("operationComponent");
                if (operation.getBooleanField("isSubcontracting")) {
                    addProductQuantitiesToList(productComponentQuantity, productQuantities);
                }
            }
        }
        return productQuantities;
    }

    private void addProductQuantitiesToList(final Entry<Entity, BigDecimal> productComponentQuantity,
            final Map<Entity, BigDecimal> productQuantities) {
        Entity product = productComponentQuantity.getKey().getBelongsToField("product");
        BigDecimal newQty = productComponentQuantity.getValue();

        BigDecimal oldQty = productQuantities.get(product);
        if (oldQty != null) {
            newQty = newQty.add(oldQty);

        }
        productQuantities.put(product, newQty);
    }

}
