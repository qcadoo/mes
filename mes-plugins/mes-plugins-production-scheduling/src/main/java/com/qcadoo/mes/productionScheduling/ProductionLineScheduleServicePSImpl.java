/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionScheduling;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.ProductionLineSchedulePositionFieldsLCNFO;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePS;
import com.qcadoo.mes.orders.listeners.ProductionLinePositionNewData;
import com.qcadoo.mes.productionScheduling.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Service
@Order(1)
@RunIfEnabled(ProductionSchedulingConstants.PLUGIN_IDENTIFIER)
public class ProductionLineScheduleServicePSImpl implements ProductionLineScheduleServicePS {

    @Autowired
    private ProductionSchedulingService productionSchedulingService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    public void createProductionLinePositionNewData(Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData,
                                                    Entity productionLine, Date finishDate, Entity order, Entity technology, Entity previousOrder) {
        Entity changeover = lineChangeoverNormsForOrdersService.getChangeover(previousOrder, technology, productionLine);
        if (changeover != null) {
            finishDate = Date.from(finishDate.toInstant().plusSeconds(changeover.getIntegerField(LineChangeoverNormsFields.DURATION)));
        }

        Entity orderTimeCalculation = productionSchedulingService.scheduleOperationsInOrder(order, technology, finishDate, productionLine);

        ProductionLinePositionNewData productionLinePositionNewData = new ProductionLinePositionNewData(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM),
                orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO), changeover);
        orderProductionLinesPositionNewData.put(productionLine.getId(), productionLinePositionNewData);
    }

    @Override
    public void savePosition(Entity position, ProductionLinePositionNewData productionLinePositionNewData) {
        position.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, productionLinePositionNewData.getChangeover());
        position.getDataDefinition().fastSave(position);
    }

    @Override
    public void copyPS() {
    }
}
