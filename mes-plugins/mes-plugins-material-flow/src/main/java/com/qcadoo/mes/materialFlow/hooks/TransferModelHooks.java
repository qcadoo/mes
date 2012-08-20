/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelHooks {

    public void copyProductionOrConsumptionDataFromBelongingTransformation(final DataDefinition dd, final Entity transfer) {
        Entity transformations = transfer.getBelongsToField(TRANSFORMATIONS_PRODUCTION);

        if (transformations == null) {
            transformations = transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION);

            if (transformations == null) {
                // came here from plain transfer detail view
                return;
            } else {
                transfer.setField(TYPE, CONSUMPTION.getStringValue());
                transfer.setField(LOCATION_FROM, transformations.getBelongsToField(LOCATION_FROM));
            }
        } else {
            transfer.setField(TYPE, PRODUCTION.getStringValue());
            transfer.setField(LOCATION_TO, transformations.getBelongsToField(LOCATION_TO));
        }

        transfer.setField(TIME, transformations.getField(TIME));
        transfer.setField(STAFF, transformations.getBelongsToField(STAFF));
    }

}
