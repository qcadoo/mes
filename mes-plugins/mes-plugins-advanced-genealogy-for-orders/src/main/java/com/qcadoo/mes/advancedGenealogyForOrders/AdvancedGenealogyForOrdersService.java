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
package com.qcadoo.mes.advancedGenealogyForOrders;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.AdvancedGenealogyForOrdersConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;

@Service
public class AdvancedGenealogyForOrdersService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

    private DataDefinition getDataDefinitionForProductInComponent() {
        return dataDefinitionService.get(AdvancedGenealogyForOrdersConstants.PLUGIN_IDENTIFIER,
                AdvancedGenealogyForOrdersConstants.MODEL_PRODUCT_IN_COMPONENT);
    }

    public final List<Entity> getExistingProductInComponents(final Entity trackingRecord) {
        if (trackingRecord.getId() == null) {
            return new ArrayList<Entity>();
        }
        SearchCriteriaBuilder search = getDataDefinitionForProductInComponent().find();
        search.add(SearchRestrictions.belongsTo("trackingRecord", trackingRecord));
        return search.list().getEntities();
    }

    public final List<Entity> buildProductInComponentList(final Entity order) {
        EntityTree operationsTree = order.getBelongsToField(OrderFields.TECHNOLOGY).getTreeField(
                TechnologyFields.OPERATION_COMPONENTS);
        return buildGenealogyProductInComponentsList(operationsTree);
    }

    private List<Entity> buildGenealogyProductInComponentsList(final EntityTree operationsTree) {
        List<Entity> genealogyProductInComponents = Lists.newArrayList();
        List<Entity> operationsList = entityTreeUtilsService.getSortedEntities(operationsTree);
        for (Entity technologyOperationComponent : operationsList) {
            List<Entity> operationProductInComponents = technologyOperationComponent
                    .getHasManyField("operationProductInComponents");
            if (operationProductInComponents == null) {
                continue;
            }
            for (Entity operationProductInComponent : operationProductInComponents) {
                genealogyProductInComponents.add(createAdvancedGenealogyProductInComponent(technologyOperationComponent,
                        operationProductInComponent));
            }
        }
        return genealogyProductInComponents;
    }

    public static final boolean isTrackingRecordForOrder(final Entity trackingRecord) {
        checkArgument("trackingRecord".equals(trackingRecord.getDataDefinition().getName()));
        return "02forOrder".equals(trackingRecord.getStringField("entityType"));
    }

    private Entity createAdvancedGenealogyProductInComponent(final Entity technologyOperationComponent,
            final Entity operationProductInComponent) {
        Entity advancedGenealogyProductInComponent = getDataDefinitionForProductInComponent().create();
        advancedGenealogyProductInComponent.setField("technologyOperationComponent", technologyOperationComponent);
        advancedGenealogyProductInComponent.setField("productInComponent", operationProductInComponent);
        advancedGenealogyProductInComponent.setField("productInBatches", new ArrayList<Entity>());
        return advancedGenealogyProductInComponent;
    }
}
