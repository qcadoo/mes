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
package com.qcadoo.mes.productionPerShift;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class PPSHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Long getPpsIdForOrder(final Long orderId) {
        DataDefinition ppsDateDef = getProductionPerShiftDD();
        String query = "select id as ppsId from #productionPerShift_productionPerShift where order.id = :orderId";
        Entity projectionResults = ppsDateDef.find(query).setLong("orderId", orderId).setMaxResults(1).uniqueResult();

        if (projectionResults == null) {
            return null;
        }

        return (Long) projectionResults.getField("ppsId");
    }

    public Long createPpsForOrderAndReturnId(final Long orderId) {
        DataDefinition productionPerShiftDD = getProductionPerShiftDD();

        Entity productionPerShift = productionPerShiftDD.create();
        productionPerShift.setField(ProductionPerShiftFields.ORDER, orderId);
        productionPerShift.setField(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE, ProgressType.PLANNED.getStringValue());

        return productionPerShiftDD.save(productionPerShift).getId();
    }

    public Entity createDailyProgressWithShift(final Entity shiftEntity) {
        Entity dailyProgress = getDailyProgressDD().create();
        dailyProgress.setField(DailyProgressFields.SHIFT, shiftEntity);
        return dailyProgress;
    }

    private DataDefinition getProductionPerShiftDD() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT);
    }

    private DataDefinition getDailyProgressDD() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_DAILY_PROGRESS);
    }
}
