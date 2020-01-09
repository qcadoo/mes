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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogyForOrders.constants.ParameterFieldsAGFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterValidatorsAGFO {

    public final boolean checkIfTrackingRecordForOrderTreatmentIsSelected(final DataDefinition parameterDD,
            final Entity parameter) {
        String trackingRecordForOrderTreatment = parameter
                .getStringField(ParameterFieldsAGFO.TRACKING_RECORD_FOR_ORDER_TREATMENT);

        if (trackingRecordForOrderTreatment == null) {
            parameter.addError(parameterDD.getField(ParameterFieldsAGFO.TRACKING_RECORD_FOR_ORDER_TREATMENT),
                    "basic.parameter.message.trackingRecordForOrderTreatmentIsNotSelected");

            return false;
        }

        return true;
    }

    public final boolean checkIfNumberPatternIsSelected(final DataDefinition parameterDD, final Entity parameter) {
        if (parameter.getBooleanField(ParameterFieldsAGFO.GENERATE_BATCH_FOR_ORDERED_PRODUCT)
                && parameter.getBelongsToField(ParameterFieldsAGFO.NUMBER_PATTERN) == null) {
            parameter.addError(parameterDD.getField(ParameterFieldsAGFO.NUMBER_PATTERN),
                    "basic.parameter.message.numberPatternIsNotSelected");
            return false;
        }

        return true;
    }

}
