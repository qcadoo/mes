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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.states.constants.OrderState.PENDING;

@Service
public class OrderModelHooksAGFO {

    private static final String L_TRACKING_RECORD_TREATMENT = "trackingRecordTreatment";

    private static final String L_TRACKING_RECORD_FOR_ORDER_TREATMENT = "trackingRecordForOrderTreatment";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void checkIfTrackingRecordTreatmentIsEmpty(final DataDefinition orderDD, final Entity order) {
        String trackingRecordTreatment = order.getStringField(L_TRACKING_RECORD_TREATMENT);

        if (trackingRecordTreatment == null) {
            order.setField(L_TRACKING_RECORD_TREATMENT, getTrackingRecordForOrderTreatment());
        }
    }

    public String getTrackingRecordForOrderTreatment() {
        Entity parameter = parameterService.getParameter();
        return parameter.getStringField(L_TRACKING_RECORD_FOR_ORDER_TREATMENT);
    }

    public final void deleteOrderFromTrackingRecord(final DataDefinition orderDD, final Entity order) {
        Entity tech = order.getBelongsToField(TECHNOLOGY);
        String state = order.getStringField(STATE);

        if (hasTechnologyChanged(order, tech) && PENDING.getStringValue().equals(state)) {
            DataDefinition tRDD = dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                    AdvancedGenealogyConstants.MODEL_TRACKING_RECORD);
            List<Entity> trackingRecordsList = tRDD.find().add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

            if (!trackingRecordsList.isEmpty()) {
                for (Entity trackingRecord : trackingRecordsList) {
                    trackingRecord.getDataDefinition().delete(trackingRecord.getId());
                }
            }
        }
    }

    private boolean hasTechnologyChanged(final Entity order, final Entity technology) {
        Entity existingOrder = getExistingOrder(order);
        if (existingOrder == null || technology == null) {
            return false;
        }
        Long existingOrderTechnology = existingOrder.getLongField(TECHNOLOGY);
        if (existingOrderTechnology == null) {
            return true;
        }
        return !existingOrderTechnology.equals(technology.getId());
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT ord.id as id, technology.id as technology ");
        query.append("FROM #orders_order ord WHERE id = :id");
        Entity orderDB = order.getDataDefinition().find(query.toString()).setLong("id", order.getId()).setMaxResults(1).uniqueResult();
        return orderDB;
    }

}
