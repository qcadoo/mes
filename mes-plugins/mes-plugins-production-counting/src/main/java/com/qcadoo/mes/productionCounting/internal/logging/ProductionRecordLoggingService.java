/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.productionCounting.internal.logging;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;

@Service
public class ProductionRecordLoggingService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    public void logCreateEvent(final DataDefinition productionRecordDD, final Entity productionRecord) {
        final Date logDate = new Date();
        final Entity logEntity = createStateLog();

        logEntity.setField("currentState", ProductionCountingStates.DRAFT.getStringValue());
        logEntity.setField("worker", securityService.getCurrentUserName());
        logEntity.setField("dateAndTime", logDate);

        productionRecord.setField("loggings", Lists.newArrayList(logEntity));
    }

    public void logStateChange(final Entity productionRecord, final ProductionCountingStates oldState,
            final ProductionCountingStates newState) {
        final Date logDate = new Date();
        final Entity logEntity = createStateLog();

        logEntity.setField("previousState", oldState.getStringValue());
        logEntity.setField("currentState", newState.getStringValue());
        logEntity.setField("worker", securityService.getCurrentUserName());
        logEntity.setField("dateAndTime", logDate);
        logEntity.setField("productionRecord", productionRecord);
        logEntity.getDataDefinition().save(logEntity);
    }

    private Entity createStateLog() {
        return getLoggingDataDefinition().create();
    }

    private DataDefinition getLoggingDataDefinition() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_RECORD_LOGGING);
    }

}
