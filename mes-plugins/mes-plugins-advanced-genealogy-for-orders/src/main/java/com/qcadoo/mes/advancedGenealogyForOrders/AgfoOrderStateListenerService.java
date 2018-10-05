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

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordForOrderTreatment;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

@Service
public class AgfoOrderStateListenerService {

    private static final String L_TRACKING_RECORD_TREATMENT = "trackingRecordTreatment";

    private static final String L_STATE = "state";

    public void onAccepted(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        if (isUnchangablePlanAfterOrderAccept(order) && !isOrderCompatibleWithUnchangeablePlan(order)) {
            stateChangeContext.addMessage("orders.order.trackingRecords.atLeastOneDraftOrNoneAcceptedError",
                    StateMessageType.FAILURE);
        }
    }

    public void onInProgress(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        if (isUnchangablePlanAfterOrderStart(order) && !isOrderCompatibleWithUnchangeablePlan(order)) {
            stateChangeContext.addMessage("orders.order.trackingRecords.atLeastOneDraftOrNoneAcceptedError",
                    StateMessageType.FAILURE);
        }
    }

    private boolean isUnchangablePlanAfterOrderAccept(final Entity order) {
        return TrackingRecordForOrderTreatment.UNCHANGABLE_PLAN_AFTER_ORDER_ACCEPT.getStringValue().equals(
                order.getStringField(L_TRACKING_RECORD_TREATMENT));
    }

    private boolean isUnchangablePlanAfterOrderStart(final Entity order) {
        return TrackingRecordForOrderTreatment.UNCHANGABLE_PLAN_AFTER_ORDER_START.getStringValue().equals(
                order.getStringField(L_TRACKING_RECORD_TREATMENT));
    }

    private boolean isOrderCompatibleWithUnchangeablePlan(final Entity order) {
        List<Entity> trackingRecords = order.getHasManyField("trackingRecords");
        return containsAtLeastOneAccepted(trackingRecords) && notContainsDrafts(trackingRecords);
    }

    private boolean containsAtLeastOneAccepted(final List<Entity> trackingRecords) {
        for (Entity trackingRecord : trackingRecords) {
            if (TrackingRecordState.ACCEPTED.getStringValue().equals(trackingRecord.getStringField(L_STATE))) {
                return true;
            }
        }
        return false;
    }

    private boolean notContainsDrafts(final List<Entity> trackingRecords) {
        for (Entity trackingRecord : trackingRecords) {
            if (TrackingRecordState.DRAFT.getStringValue().equals(trackingRecord.getStringField(L_STATE))) {
                return false;
            }
        }
        return true;
    }
}
