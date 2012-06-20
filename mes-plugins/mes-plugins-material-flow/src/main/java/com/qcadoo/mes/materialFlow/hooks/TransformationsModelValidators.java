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

import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
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

import com.qcadoo.mes.materialFlow.MaterialFlowResourceService;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.TransformationsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class TransformationsModelValidators {

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private MaterialFlowResourceService materialFlowResourceService;

    public boolean checkIfTransfersAreValid(final DataDefinition transformationsDD, final Entity transformations) {
        Entity stockAreasFrom = transformations.getBelongsToField(TransformationsFields.STOCK_AREAS_FROM);

        List<Entity> transfersConsumption = transformations.getHasManyField(TRANSFERS_CONSUMPTION);
        List<Entity> transfersProduction = transformations.getHasManyField(TRANSFERS_PRODUCTION);

        return (checkIfTransfersAreValid(transfersConsumption, CONSUMPTION.getStringValue(), stockAreasFrom) && checkIfTransfersAreValid(
                transfersProduction, PRODUCTION.getStringValue(), null))
                && (checkIfTransfersNumbersAreDistinct(transfersConsumption, transfersProduction) && checkIfTransfersNumbersAreDistinct(
                        transfersProduction, transfersConsumption));
    }

    private boolean checkIfTransfersAreValid(final List<Entity> transfers, final String type, final Entity stockAreasFrom) {
        boolean isValid = true;

        for (Entity transfer : transfers) {
            String number = transfer.getStringField(NUMBER);
            Entity product = transfer.getBelongsToField(PRODUCT);
            BigDecimal quantity = transfer.getDecimalField(QUANTITY);

            if ((number == null) || number.isEmpty()) {
                appendErrorToModelField(transfer, NUMBER, "materialFlow.multitransfer.validation.fieldRequired");

                isValid = false;
            }

            if (product == null) {
                appendErrorToModelField(transfer, PRODUCT, "materialFlow.multitransfer.validation.fieldRequired");

                isValid = false;
            } else {
                if (isProductAlreadyAdded(transfers, product)) {
                    appendErrorToModelField(transfer, PRODUCT, "materialFlow.multitransfer.validation.productAlreadyAdded");

                    isValid = false;
                }
            }

            if (quantity == null) {
                appendErrorToModelField(transfer, QUANTITY, "materialFlow.multitransfer.validation.fieldRequired");

                isValid = false;
            }

            if (CONSUMPTION.getStringValue().equals(type) && (stockAreasFrom != null) && (product != null) && (quantity != null)
                    && !materialFlowResourceService.areResourcesSufficient(stockAreasFrom, product, quantity)) {
                appendErrorToModelField(transfer, QUANTITY, "materialFlow.multitransfer.validation.resourcesArentSufficient");

                isValid = false;
            }
        }

        return isValid;
    }

    private boolean checkIfTransfersNumbersAreDistinct(final List<Entity> transfersConsumption,
            final List<Entity> transfersProduction) {
        boolean isValid = true;

        for (Entity transfer : transfersConsumption) {
            if (transfer.getId() == null) {
                String number = transfer.getStringField(NUMBER);

                if ((number != null)
                        && (((isNumberAlreadyUsed(transfersConsumption, number) + isNumberAlreadyUsed(transfersProduction, number)) > 1) || materialFlowService
                                .numberAlreadyExist(MaterialFlowConstants.MODEL_TRANSFER, number))) {
                    appendErrorToModelField(transfer, NUMBER, "materialFlow.multitransfer.validation.numberAlreadyUsed");

                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean isProductAlreadyAdded(final List<Entity> transfers, final Entity product) {
        if (product == null) {
            return false;
        }

        int count = 0;

        for (Entity transfer : transfers) {
            Entity productAlreadyAdded = transfer.getBelongsToField(PRODUCT);

            if (product.equals(productAlreadyAdded)) {
                count++;

                if (count > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    private int isNumberAlreadyUsed(final List<Entity> transfers, final String number) {
        if (number == null) {
            return 0;
        }

        int count = 0;

        for (Entity transfer : transfers) {
            String numberAlreadyUsed = transfer.getStringField(NUMBER);

            if (number.equals(numberAlreadyUsed)) {
                count++;

                if (count > 1) {
                    return count;
                }
            }
        }

        return count;
    }

    private void appendErrorToModelField(final Entity entity, final String fieldName, final String messageKey) {
        FieldDefinition productInFieldDef = entity.getDataDefinition().getField(fieldName);
        entity.addError(productInFieldDef, messageKey);
    }

}
