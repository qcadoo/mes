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

import static com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordForOrderTreatment.UNCHANGABLE_PLAN_AFTER_ORDER_ACCEPT;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordForOrderTreatment;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginManager;

@Service
public class OrderModelValidators {

    private static final String L_TRACKING_RECORD_TREATMENT = "trackingRecordTreatment";

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ParameterService parameterService;

    public final boolean checkIfTrackingRecordTreatmentIsSelected(final DataDefinition orderDD, final Entity order) {
        String trackingRecordTreatment = order.getStringField(L_TRACKING_RECORD_TREATMENT);

        if (trackingRecordTreatment == null) {
            order.addError(orderDD.getField(L_TRACKING_RECORD_TREATMENT),
                    "orders.order.message.trackingRecordTreatmentIsNotSelected");

            return false;
        }
        return true;
    }

    public final boolean checkSelectedTrackingRecordForOrderTreatment(final DataDefinition orderDD, final Entity order) {
        if (!pluginManager.isPluginEnabled("srcAdvGenealogyForOrders")) {
            return true;
        }
        String trackingRecordTreatment = order.getStringField(L_TRACKING_RECORD_TREATMENT);
        if (trackingRecordTreatment == null) {
            Entity parameter = parameterService.getParameter();
            if (parameter.getStringField("trackingRecordForOrderTreatment") == null) {
                order.setField(L_TRACKING_RECORD_TREATMENT, TrackingRecordForOrderTreatment.DURING_PRODUCTION.getStringValue());
            } else {
                order.setField(L_TRACKING_RECORD_TREATMENT, parameter.getStringField("trackingRecordForOrderTreatment"));
            }
        }
        if (!StringUtils.isEmpty(order.getStringField(OrderFields.EXTERNAL_NUMBER))
                && order.getStringField(L_TRACKING_RECORD_TREATMENT).equals(UNCHANGABLE_PLAN_AFTER_ORDER_ACCEPT.getStringValue())) {
            order.addError(order.getDataDefinition().getField(L_TRACKING_RECORD_TREATMENT),
                    "srcAdvGenealogy.trackingRecordForOrderTreatment.inOrder");
            return false;
        }
        return true;
    }
}
