/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.basicProductionCounting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.states.ChangeOrderStateMessage;
import com.qcadoo.mes.orders.states.OrderStateListener;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BasicProductionCountingOrderStatesListener extends OrderStateListener {

    @Autowired
    private MaterialRequirementReportDataService materialRequirementReportDataService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<ChangeOrderStateMessage> onAccepted(final Entity newEntity) {
        Preconditions.checkArgument(newEntity != null, "Order is null");
        final Entity order = newEntity.getDataDefinition().get(newEntity.getId());

        final List<Entity> prodCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        List<ChangeOrderStateMessage> messages = new ArrayList<ChangeOrderStateMessage>();

        if (prodCountings == null || prodCountings.isEmpty()) {
            final Map<Entity, BigDecimal> productsReq = materialRequirementReportDataService
                    .getQuantitiesForOrdersTechnologyProducts(Arrays.asList(order), false);

            for (Entry<Entity, BigDecimal> productReq : productsReq.entrySet()) {
                Entity productionCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
                productionCounting.setField("order", order);
                productionCounting.setField("product", productReq.getKey());
                productionCounting.setField("plannedQuantity", productReq.getValue());
                productionCounting = productionCounting.getDataDefinition().save(productionCounting);
                if (!productionCounting.isValid()) {
                    throw new IllegalStateException("Saved order entity is invalid.");
                }
            }

            Entity productionCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
            productionCounting.setField("order", order);
            productionCounting.setField("product", order.getBelongsToField("product"));
            productionCounting.setField("plannedQuantity", order.getField("plannedQuantity"));
            productionCounting = productionCounting.getDataDefinition().save(productionCounting);
            if (!productionCounting.isValid()) {
                throw new IllegalStateException("Saved order entity is invalid.");
            }
        }

        return messages;
    }
}