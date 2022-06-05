/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.productionPerShift.services;

import java.util.Date;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

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

    public void createProductionLinePositionNewData(Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData,
                                                    Entity productionLine, Date finishDate, Entity order) {
        order.setField(OrderFields.START_DATE, finishDate);
        order.setField(OrderFields.PRODUCTION_LINE, productionLine);
        order = order.getDataDefinition().save(order);

        ProductionLinePositionNewData productionLinePositionNewData = new ProductionLinePositionNewData(finishDate,
                order.getDateField(OrderFields.FINISH_DATE));
        orderProductionLinesPositionNewData.put(productionLine.getId(), productionLinePositionNewData);
    }

}
