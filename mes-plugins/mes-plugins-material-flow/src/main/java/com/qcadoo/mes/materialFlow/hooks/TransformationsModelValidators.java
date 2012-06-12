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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class TransformationsModelValidators {

    public boolean checkIfProductsInTransfersConsumptionAreDistinct(final DataDefinition transformationsDD,
            final Entity transformations) {
        List<Entity> transfersConsumption = transformations.getHasManyField(TRANSFERS_CONSUMPTION);

        return checkIfProductInTransfersAreDistinct(transfersConsumption);
    }

    public boolean checkIfProductsInTransfersProductionAreDistinct(final DataDefinition transformationsDD,
            final Entity transformations) {
        List<Entity> transfersProduction = transformations.getHasManyField(TRANSFERS_PRODUCTION);

        return checkIfProductInTransfersAreDistinct(transfersProduction);
    }

    private boolean checkIfProductInTransfersAreDistinct(final List<Entity> transfers) {
        boolean isValid = true;
        for (Entity transfer : transfers) {
            Entity product = transfer.getBelongsToField(PRODUCT);
            if (isProductAlreadyAdded(transfers, product)) {
                appendErrorToModelField(transfer, PRODUCT, "technologies.productComponent.error.productAlreadyAdded");
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean isProductAlreadyAdded(final List<Entity> transfers, final Entity product) {
        if (product == null) {
            return false;
        }
        int count = 0;
        for (Entity productInComponent : transfers) {
            Entity productAlreadyAdded = productInComponent.getBelongsToField(PRODUCT);
            if (product.equals(productAlreadyAdded)) {
                count++;
                if (count > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private void appendErrorToModelField(final Entity entity, final String fieldName, final String messageKey) {
        FieldDefinition productInFieldDef = entity.getDataDefinition().getField(fieldName);
        entity.addError(productInFieldDef, messageKey);
    }

}
