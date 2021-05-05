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
package com.qcadoo.mes.productFlowThruDivision.states.aop.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.states.ProductionTrackingListenerServicePFTD;
import com.qcadoo.mes.productionCounting.newstates.ProductionTrackingStateServiceMarker;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeDescriber;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Service
@RunIfEnabled(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER)
public class ProductionTrackingStateServicePFTD extends BasicStateService implements ProductionTrackingStateServiceMarker {

    @Autowired
    private ProductionTrackingListenerServicePFTD productionTrackingListenerService;

    @Autowired
    private ProductionTrackingStateChangeDescriber productionTrackingStateChangeDescriber;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return productionTrackingStateChangeDescriber;
    }

    @Override
    public Entity onBeforeSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity, StateChangeEntityDescriber describer) {
        switch (targetState) {
            case ProductionTrackingStateStringValues.ACCEPTED:
                productionTrackingListenerService.onAccept(entity, sourceState);
                break;
        }

        return entity;
    }

}
