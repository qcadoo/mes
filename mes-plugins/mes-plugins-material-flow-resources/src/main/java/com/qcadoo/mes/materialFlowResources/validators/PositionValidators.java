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
package com.qcadoo.mes.materialFlowResources.validators;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementServiceImpl.WarehouseAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PositionValidators {

    public boolean checkAttributesRequirement(final DataDefinition dataDefinition, final Entity position) {

        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);

        DocumentType documentType = DocumentType.of(document);
        DocumentState documentState = DocumentState.of(document);

        if (documentState == DocumentState.ACCEPTED
                && (documentType == DocumentType.RECEIPT || documentType == DocumentType.INTERNAL_INBOUND)) {
            Entity warehouseTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
            return validatePositionAttributes(dataDefinition, position,
                    warehouseTo.getBooleanField(LocationFieldsMFR.REQUIRE_PRICE),
                    warehouseTo.getBooleanField(LocationFieldsMFR.REQUIRE_BATCH),
                    warehouseTo.getBooleanField(LocationFieldsMFR.REQUIRE_PRODUCTION_DATE),
                    warehouseTo.getBooleanField(LocationFieldsMFR.REQUIRE_EXPIRATION_DATE));
        }
        if (documentState == DocumentState.ACCEPTED
                && (documentType == DocumentType.RELEASE || documentType == DocumentType.TRANSFER)) {
            Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            return validateResourceAttribute(dataDefinition, position, warehouseFrom);
        }
        return true;
    }

    private boolean validateResourceAttribute(DataDefinition dataDefinition, Entity position, Entity warehouseFrom) {
        String algorithm = warehouseFrom.getStringField(LocationFieldsMFR.ALGORITHM);
        boolean result = (algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue()) && position
                .getField(PositionFields.RESOURCE) != null)
                || !algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue());
        if (!result) {
            position.addError(dataDefinition.getField(PositionFields.RESOURCE), "materialFlow.error.position.batch.required");
        }
        return result;
    }

    private boolean validatePositionAttributes(DataDefinition dataDefinition, Entity position, boolean requirePrice,
            boolean requireBatch, boolean requireProductionDate, boolean requireExpirationDate) {

        boolean result = true;
        if (requirePrice && position.getField(PositionFields.PRICE) == null) {
            position.addError(dataDefinition.getField(PositionFields.PRICE), "materialFlow.error.position.price.required");
            result = false;
        }
        if (requireBatch && position.getField(PositionFields.BATCH) == null) {
            position.addError(dataDefinition.getField(PositionFields.BATCH), "materialFlow.error.position.batch.required");
            result = false;
        }
        if (requireProductionDate && position.getField(PositionFields.PRODUCTION_DATE) == null) {
            position.addError(dataDefinition.getField(PositionFields.PRODUCTION_DATE),
                    "materialFlow.error.position.productionDate.required");
            result = false;
        }

        if (requireExpirationDate && position.getField(PositionFields.EXPIRATION_DATE) == null) {
            position.addError(dataDefinition.getField(PositionFields.EXPIRATION_DATE),
                    "materialFlow.error.position.expirationDate.required");
            result = false;
        }

        return result;
    }

    public boolean validateDates(final DataDefinition dataDefinition, final Entity position) {

        Date productionDate = position.getDateField(PositionFields.PRODUCTION_DATE);
        Date expirationDate = position.getDateField(PositionFields.EXPIRATION_DATE);
        if (productionDate != null && expirationDate != null && expirationDate.compareTo(productionDate) < 0) {
            position.addError(dataDefinition.getField(PositionFields.EXPIRATION_DATE),
                    "materialFlow.error.position.expirationDate.lessThenProductionDate");
            return false;
        }

        return true;
    }
}
