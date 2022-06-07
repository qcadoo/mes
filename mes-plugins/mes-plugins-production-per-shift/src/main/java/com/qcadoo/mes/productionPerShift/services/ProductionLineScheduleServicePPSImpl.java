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
package com.qcadoo.mes.productionPerShift.services;

import java.util.Date;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.ProductionLineSchedulePositionFieldsLCNFO;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePPS;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.listeners.ProductionLinePositionNewData;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Service
@Order(1)
@RunIfEnabled(ProductionPerShiftConstants.PLUGIN_IDENTIFIER)
public class ProductionLineScheduleServicePPSImpl implements ProductionLineScheduleServicePPS {

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Autowired
    private ShiftsService shiftsService;

    public void createProductionLinePositionNewData(Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData,
                                                    Entity productionLine, Date finishDate, Entity order, Entity technology, Entity previousOrder) {
        Entity changeover = lineChangeoverNormsForOrdersService.getChangeover(previousOrder, technology, productionLine);
        if (changeover != null) {
            finishDate = Date.from(finishDate.toInstant().plusSeconds(changeover.getIntegerField(LineChangeoverNormsFields.DURATION)));
        }

        DateTime finishDateTime = new DateTime(finishDate);
        finishDate = shiftsService
                .getNearestWorkingDate(finishDateTime, productionLine).orElse(finishDateTime).toDate();

        order.setField(OrderFields.START_DATE, finishDate);
        order.setField(OrderFields.PRODUCTION_LINE, productionLine);
        order.setField(OrderFields.GENERATE_PPS, true);
        order = order.getDataDefinition().save(order);

        ProductionLinePositionNewData productionLinePositionNewData = new ProductionLinePositionNewData(finishDate,
                order.getDateField(OrderFields.FINISH_DATE), changeover);
        orderProductionLinesPositionNewData.put(productionLine.getId(), productionLinePositionNewData);
    }

    @Override
    public void savePosition(Entity position, ProductionLinePositionNewData productionLinePositionNewData) {
        position.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, productionLinePositionNewData.getChangeover());
        position.getDataDefinition().fastSave(position);
    }
}
