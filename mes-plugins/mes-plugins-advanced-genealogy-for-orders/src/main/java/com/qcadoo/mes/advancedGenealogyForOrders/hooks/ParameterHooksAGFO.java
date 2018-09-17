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
import com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordForOrderTreatment;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksAGFO {

    public void addFieldsForParameter(final DataDefinition parameterDD, final Entity parameter) {
        parameter.setField(ParameterFieldsAGFO.TRACKING_RECORD_FOR_ORDER_TREATMENT,
                TrackingRecordForOrderTreatment.DURING_PRODUCTION.getStringValue());
        parameter.setField(ParameterFieldsAGFO.BATCH_NUMBER_REQUIRED_PRODUCTS, false);
    }

}
