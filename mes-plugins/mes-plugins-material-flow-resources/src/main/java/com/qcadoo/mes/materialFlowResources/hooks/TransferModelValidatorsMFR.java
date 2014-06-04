/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelValidatorsMFR {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public boolean validateTransferDate(final DataDefinition transferDD, final Entity transfer) {
        Date time = (Date) transfer.getField(TIME);

        Entity locationFrom = transfer.getBelongsToField(LOCATION_FROM);
        Entity locationTo = transfer.getBelongsToField(LOCATION_TO);

        if (materialFlowResourcesService.shouldValidateDateWhenTransferToWarehouse()
                && materialFlowResourcesService.areLocationsWarehouses(locationFrom, locationTo)
                && !materialFlowResourcesService.isDateGraterThanResourcesDate(time)) {
            transfer.addError(transferDD.getField(TIME),
                    "materialFlowResources.validate.global.error.dateEarlierThanResourcesDate");

            return false;
        }

        return true;
    }

    public boolean validateTransferResources(final DataDefinition transferDD, final Entity transfer) {
        boolean validate = true;

        String type = transfer.getStringField(TYPE);
        Entity locationFrom = transfer.getBelongsToField(LOCATION_FROM);
        Entity product = transfer.getBelongsToField(PRODUCT);
        BigDecimal quantity = transfer.getDecimalField(QUANTITY);

        if ((type != null) && !PRODUCTION.getStringValue().equals(type) && (locationFrom != null) && (product != null)
                && (quantity != null) && !materialFlowResourcesService.areResourcesSufficient(locationFrom, product, quantity)) {
            transfer.addError(transferDD.getField(QUANTITY),
                    "materialFlowResources.validate.global.error.resourcesArentSufficient");

            validate = false;
        }

        return validate;
    }
}
