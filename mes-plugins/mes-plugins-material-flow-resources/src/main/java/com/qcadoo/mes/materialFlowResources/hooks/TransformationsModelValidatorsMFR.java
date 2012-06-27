/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.constants.TransformationsFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class TransformationsModelValidatorsMFR {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public boolean checkIfTransfersResourcesAreValid(final DataDefinition transformationsDD, final Entity transformations) {
        Entity locationFrom = transformations.getBelongsToField(TransformationsFields.LOCATION_FROM);

        List<Entity> transfersConsumption = transformations.getHasManyField(TRANSFERS_CONSUMPTION);
        List<Entity> transfersProduction = transformations.getHasManyField(TRANSFERS_PRODUCTION);

        return (checkIfTransfersAreValid(transfersConsumption, CONSUMPTION.getStringValue(), locationFrom) && checkIfTransfersAreValid(
                transfersProduction, PRODUCTION.getStringValue(), null));
    }

    private boolean checkIfTransfersAreValid(final List<Entity> transfers, final String type, final Entity locationFrom) {
        boolean isValid = true;

        for (Entity transfer : transfers) {
            Entity product = transfer.getBelongsToField(PRODUCT);
            BigDecimal quantity = transfer.getDecimalField(QUANTITY);

            if (CONSUMPTION.getStringValue().equals(type) && (locationFrom != null) && (product != null) && (quantity != null)
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
