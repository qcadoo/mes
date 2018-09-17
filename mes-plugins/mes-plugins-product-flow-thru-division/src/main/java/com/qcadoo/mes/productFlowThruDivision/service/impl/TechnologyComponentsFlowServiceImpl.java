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
package com.qcadoo.mes.productFlowThruDivision.service.impl;

import static com.qcadoo.model.api.search.SearchProjections.*;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eqField;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.service.TechnologyComponentsFlowService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;

@Service
public class TechnologyComponentsFlowServiceImpl implements TechnologyComponentsFlowService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    @Override
    public Optional<Entity> getComponentLocation(Entity technology, Long productId) {
        Preconditions.checkArgument(productId != null, "Product id is required");

        return getComponentLocation(technology,
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId));
    }

    @Override
    public Optional<Entity> getComponentLocation(Entity technology, Entity product) {
        Preconditions.checkArgument(technology != null, "Technology is required.");
        Preconditions.checkArgument(technology != null, "Product is required.");

        DataDefinition operationProductInComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

        Entity operationProductInComponent = operationProductInComponentDD.find()
                .createAlias(OperationProductInComponentFields.OPERATION_COMPONENT, "c", JoinType.INNER)
                .add(belongsTo(OperationProductInComponentFields.PRODUCT, product))
                .add(belongsTo("c." + TechnologyOperationComponentFields.TECHNOLOGY, technology)).setMaxResults(1).uniqueResult();

        if (technologyService.isIntermediateProduct(operationProductInComponent)) {
            return Optional.absent();
        }

        return Optional.fromNullable(operationProductInComponent
                .getBelongsToField(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION));
    }

    @Override
    public Map<Long, BigDecimal> getComponentsStock(Entity technology, boolean externalNumberShouldBeNull) {
        Preconditions.checkArgument(technology != null, "Technology is required.");

        DataDefinition operationProductInComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

        SearchCriteriaBuilder scb = operationProductInComponentDD.find()
                .createAlias(OperationProductInComponentFields.OPERATION_COMPONENT, "c", JoinType.INNER)
                .createAlias(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION, "w", JoinType.LEFT)
                .createAlias("w." + LocationFieldsMFR.RESOURCES, "r", JoinType.LEFT)
                .add(eqField("r." + ResourceFields.PRODUCT, OperationProductInComponentFields.PRODUCT))
                .add(belongsTo("c." + TechnologyOperationComponentFields.TECHNOLOGY, technology));

        if (externalNumberShouldBeNull) {
            scb.add(isNull("w." + LocationFields.EXTERNAL_NUMBER));
        }

        scb.setProjection(
                list().add(
                        alias(groupField(OperationProductInComponentFields.PRODUCT), OperationProductInComponentFields.PRODUCT))
                        .add(alias(sum("r." + ResourceFields.QUANTITY), ResourceFields.QUANTITY))).addOrder(
                SearchOrders.asc(ResourceFields.QUANTITY));
        List<Entity> componentsStock = scb.list().getEntities();

        Map<Long, BigDecimal> stockMap = Maps.newHashMap();
        for (Entity componentStock : componentsStock) {
            stockMap.put(componentStock.getBelongsToField(OperationProductInComponentFields.PRODUCT).getId(),
                    componentStock.getDecimalField(ResourceFields.QUANTITY));
        }

        return stockMap;
    }

    @Override
    public Map<Long, BigDecimal> getComponentsStock(Entity technology) {
        return getComponentsStock(technology, false);
    }


}
