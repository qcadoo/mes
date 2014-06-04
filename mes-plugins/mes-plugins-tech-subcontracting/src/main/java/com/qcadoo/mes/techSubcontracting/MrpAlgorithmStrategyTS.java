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
package com.qcadoo.mes.techSubcontracting;

import static com.qcadoo.mes.technologies.constants.OperationProductInComponentFields.PRODUCT;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.CHILDREN;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyOperationComponentFieldsTS;
import com.qcadoo.mes.technologies.MrpAlgorithmStrategy;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service("mrpAlgorithmStrategyTS")
public class MrpAlgorithmStrategyTS implements MrpAlgorithmStrategy {

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public boolean isApplicableFor(final MrpAlgorithm mrpAlgorithm) {
        return MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS.equals(mrpAlgorithm);
    }

    public Map<Long, BigDecimal> perform(final OperationProductComponentWithQuantityContainer productComponentWithQuantities,
            final Set<OperationProductComponentHolder> nonComponents, final MrpAlgorithm mrpAlgorithm,
            final String operationProductComponentModelName) {
        OperationProductComponentWithQuantityContainer allWithSameEntityType = productComponentWithQuantities
                .getAllWithSameEntityType(operationProductComponentModelName);

        Map<Long, BigDecimal> productWithQuantities = Maps.newHashMap();

        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentWithQuantity : allWithSameEntityType.asMap()
                .entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentWithQuantity.getKey();

            if (nonComponents.contains(operationProductComponentHolder)) {
                Entity product = operationProductComponentHolder.getProduct();
                Entity technologyOperationComponent = operationProductComponentHolder.getTechnologyOperationComponent();

                if (technologyOperationComponent != null) {
                    List<Entity> children = technologyOperationComponent.getHasManyField(CHILDREN).find()
                            .add(SearchRestrictions.eq(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING, true)).list()
                            .getEntities();

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
            }

            productQuantitiesService.addProductQuantitiesToList(productComponentWithQuantity, productWithQuantities);
        }

        return productWithQuantities;
    }

}
