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
package com.qcadoo.mes.materialFlowResources.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class TransformationsModelValidatorsMFR {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public boolean checkIfTransformationsDateIsValid(final DataDefinition transformationsDD, final Entity transformations) {
        Date time = (Date) transformations.getField(TIME);

        Entity locationFrom = transformations.getBelongsToField(LOCATION_FROM);
        Entity locationTo = transformations.getBelongsToField(LOCATION_TO);

        if (materialFlowResourcesService.shouldValidateDateWhenTransferToWarehouse()
                && materialFlowResourcesService.areLocationsWarehouses(locationFrom, locationTo)
                && !materialFlowResourcesService.isDateGraterThanResourcesDate(time)) {
            transformations.addError(transformationsDD.getField(TIME),
                    "materialFlowResources.validate.global.error.dateEarlierThanResourcesDate");

            return false;
        }

        return true;
    }

    public boolean checkIfTransfersResourcesAreValid(final DataDefinition transformationsDD, final Entity transformations) {
        return (checkIfTransfersAreValid(transformations, CONSUMPTION.getStringValue(), TRANSFERS_CONSUMPTION) && checkIfTransfersAreValid(
                transformations, PRODUCTION.getStringValue(), TRANSFERS_PRODUCTION));
    }

    private boolean checkIfTransfersAreValid(final Entity transformations, final String type, final String transfersName) {
        boolean isValid = true;

        Entity locationFrom = transformations.getBelongsToField(LOCATION_FROM);

        List<Entity> transfers = transformations.getHasManyField(transfersName);

        for (Entity transfer : transfers) {
            Entity product = transfer.getBelongsToField(PRODUCT);
            BigDecimal quantity = transfer.getDecimalField(QUANTITY);

            if ((type != null) && CONSUMPTION.getStringValue().equals(type) && (locationFrom != null) && (product != null)
                    && (quantity != null)
                    && !materialFlowResourcesService.areResourcesSufficient(locationFrom, product, quantity)) {
                appendErrorToModelField(transfer, QUANTITY,
                        "materialFlowResources.validate.global.error.resourcesArentSufficient");

                isValid = false;
            }
        }

        return isValid;
    }

    private void appendErrorToModelField(final Entity entity, final String fieldName, final String messageKey) {
        FieldDefinition productInFieldDef = entity.getDataDefinition().getField(fieldName);
        entity.addError(productInFieldDef, messageKey);
    }

}
