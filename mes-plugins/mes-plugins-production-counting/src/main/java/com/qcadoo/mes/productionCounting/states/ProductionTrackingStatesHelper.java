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
package com.qcadoo.mes.productionCounting.states;

import com.qcadoo.mes.newstates.StateExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.newstates.ProductionTrackingStateServiceMarker;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeDescriber;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionTrackingStatesHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionTrackingStatesHelper.class);

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private ProductionTrackingStateChangeDescriber stateChangeDescriber;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateExecutorService stateExecutorService;

    public void setInitialState(final Entity productionTracking) {
        stateChangeEntityBuilder.buildInitial(stateChangeDescriber, productionTracking, ProductionTrackingState.DRAFT);
    }

    public void resumeStateChange(final Entity productionTracking) {
        stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, productionTracking, ProductionTrackingState.ACCEPTED.getStringValue());
    }

    public void cancelStateChange(final Entity productionTracking) {
        stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, productionTracking, ProductionTrackingState.DRAFT.getStringValue());
    }

    public StateChangeContext findStateTransition(final Entity productionTracking) {
        Entity stateChangeEntity = findStateChangeEntity(productionTracking);

        if (stateChangeEntity == null) {
            return null;
        }

        return stateChangeContextBuilder.build(stateChangeDescriber, stateChangeEntity);
    }

    private Entity findStateChangeEntity(final Entity productionTracking) {
        DataDefinition stateChangeDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_TRACKING_STATE_CHANGE);
        SearchCriteriaBuilder scb = stateChangeDD.find();
        scb.add(SearchRestrictions.belongsTo(ProductionTrackingStateChangeFields.PRODUCTION_TRACKING, productionTracking));
        scb.add(SearchRestrictions.eq(ProductionTrackingStateChangeFields.STATUS, StateChangeStatus.SUCCESSFUL.getStringValue()));

        return scb.setMaxResults(1).uniqueResult();
    }

    public StateChangeStatus tryAccept(final Entity productionRecord, final boolean logMessages) {
        stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, productionRecord, ProductionTrackingStateStringValues.ACCEPTED);

        if (productionRecord.isValid()) {
            return StateChangeStatus.SUCCESSFUL;
        } else {
            if (logMessages) {
                LOGGER.error(productionRecord.getErrors().toString());
                LOGGER.error(productionRecord.getGlobalErrors().toString());
            }

            return StateChangeStatus.FAILURE;
        }
    }

    public StateChangeStatus tryCorrect(final Entity productionRecord, final boolean logMessages) {
        stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, productionRecord, ProductionTrackingStateStringValues.CORRECTED);

        if (productionRecord.isValid()) {
            return StateChangeStatus.SUCCESSFUL;
        } else {
            if (logMessages) {
                LOGGER.error(productionRecord.getErrors().toString());
                LOGGER.error(productionRecord.getGlobalErrors().toString());
            }

            return StateChangeStatus.FAILURE;
        }
    }

    private void logMessages(final StateChangeContext context) {
        if (!LOGGER.isWarnEnabled()) {
            return;
        }
        StringBuilder messages = new StringBuilder();
        for (Entity message : context.getAllMessages()) {
            messages.append('\t');
            messages.append(message.getStringField(MessageFields.TYPE));
            messages.append("");
            messages.append(message.getStringField(MessageFields.TRANSLATION_KEY));
            messages.append('\n');
        }
        LOGGER.warn(String.format("Production record acceptation failed. Messages: \n%s", messages.toString()));
    }

}
