/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.TransferType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelValidators {

    private static final String L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_TYPE = "materialFlow.validate.global.error.fillType";

    private static final String L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_DATE = "materialFlow.validate.global.error.fillDate";

    private static final String L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_REQUIRED = "materialFlow.validate.global.error.fillRequired";

    private static final String L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_LOCATION_HAS_EXTERNAL_NUMBER = "materialFlow.validate.global.error.locationHasExternalNumber";

    @Autowired
    private MaterialFlowService materialFlowService;

    public boolean validateTransfer(final DataDefinition transferDD, final Entity transfer) {
        boolean isValid = true;
        boolean result = true;
        String type = transfer.getStringField(TYPE);
        Date time = (Date) transfer.getField(TIME);

        if (type == null) {
            transfer.addError(transferDD.getField(TYPE), L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_TYPE);

            isValid = false;
        } else {
            result = validateLocation(transferDD, transfer, type);
        }

        if (time == null) {
            transfer.addError(transferDD.getField(TIME), L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_DATE);

            isValid = false;
        }
        if (isValid) {
            isValid = result;
        }
        return isValid;
    }

    private boolean validateLocation(final DataDefinition transferDD, final Entity transfer, final String type) {
        Entity locationFrom = transfer.getBelongsToField(LOCATION_FROM);
        Entity locationTo = transfer.getBelongsToField(LOCATION_TO);
        boolean isValid = true;
        if (type.equals(TransferType.PRODUCTION.getStringValue()) && locationTo == null) {
            transfer.addError(transferDD.getField(LOCATION_TO), L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_REQUIRED);
            isValid = false;
        }
        if (type.equals(TransferType.CONSUMPTION.getStringValue()) && locationFrom == null) {
            transfer.addError(transferDD.getField(LOCATION_FROM), L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_REQUIRED);
            isValid = false;
        }
        if (locationFrom == null && locationTo == null && (type.equals(TransferType.TRANSPORT.getStringValue()))) {
            transfer.addError(transferDD.getField(LOCATION_FROM), L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_REQUIRED);
            transfer.addError(transferDD.getField(LOCATION_TO), L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_FILL_REQUIRED);
            isValid = false;
        }
        return isValid;
    }

    public boolean checkIfLocationFromOrLocationToHasExternalNumber(final DataDefinition transferDD, final Entity transfer) {
        boolean isValid = true;

        if (materialFlowService.checkIfLocationHasExternalNumber(transfer.getBelongsToField(LOCATION_FROM))) {
            transfer.addError(transferDD.getField(LOCATION_FROM),
                    L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_LOCATION_HAS_EXTERNAL_NUMBER);

            isValid = false;
        }

        if (materialFlowService.checkIfLocationHasExternalNumber(transfer.getBelongsToField(LOCATION_TO))) {
            transfer.addError(transferDD.getField(LOCATION_TO),
                    L_MATERIAL_FLOW_VALIDATE_GLOBAL_ERROR_LOCATION_HAS_EXTERNAL_NUMBER);

            isValid = false;
        }

        return isValid;
    }

}
