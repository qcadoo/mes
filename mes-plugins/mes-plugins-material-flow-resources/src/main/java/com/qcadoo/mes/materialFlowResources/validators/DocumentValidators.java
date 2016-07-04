/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.materialFlowResources.validators;

import static com.qcadoo.mes.materialFlow.constants.LocationFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.LocationType.WAREHOUSE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class DocumentValidators {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PositionValidators positionValidators;

    public boolean validate(final DataDefinition dataDefinition, final Entity entity) {
        validateDocumentName(dataDefinition, entity);
        hasWarehouses(dataDefinition, entity);
        validateAvailableQuantities(entity);

        return entity.isValid();
    }

    public boolean hasWarehouses(final DataDefinition dataDefinition, final Entity entity) {

        DocumentType documentType = DocumentType.of(entity);
        if (DocumentType.RECEIPT.equals(documentType) || DocumentType.INTERNAL_INBOUND.equals(documentType)) {
            return hasWarehouse(dataDefinition, entity, DocumentFields.LOCATION_TO);
        } else if (DocumentType.TRANSFER.equals(documentType)) {
            return hasWarehouse(dataDefinition, entity, DocumentFields.LOCATION_FROM)
                    && hasWarehouse(dataDefinition, entity, DocumentFields.LOCATION_TO);
        } else if (DocumentType.RELEASE.equals(documentType) || DocumentType.INTERNAL_OUTBOUND.equals(documentType)) {
            return hasWarehouse(dataDefinition, entity, DocumentFields.LOCATION_FROM);
        } else {
            throw new IllegalStateException("Unknown document type.");
        }
    }

    private boolean hasWarehouse(final DataDefinition dataDefinition, final Entity entity, String warehouseField) {
        Entity location = entity.getBelongsToField(warehouseField);
        if (location == null) {
            entity.addError(dataDefinition.getField(warehouseField), "materialFlow.error.document.warehouse.required");
            return false;
        }
        if (!WAREHOUSE.getStringValue().equals(location.getStringField(TYPE))) {
            entity.addError(dataDefinition.getField(warehouseField),
                    "materialFlow.document.validate.global.error.locationIsNotWarehouse");
            return false;
        }
        return true;
    }

    private void validateDocumentName(final DataDefinition documentDD, final Entity document) {
        if (document.getId() == null) {
            return;
        }

        String documentName = document.getStringField(DocumentFields.NAME);

        if (Strings.isNullOrEmpty(documentName)) {
            document.addError(documentDD.getField(DocumentFields.NAME), "materialFlow.error.document.name.required");
        }
    }

    private boolean validateAvailableQuantities(final Entity document) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
        List<Entity> posistions = document.getHasManyField(DocumentFields.POSITIONS);
        for (Entity position : posistions) {
            if (!positionValidators.validateAvailableQuantity(positionDD, position, document)) {
                document.addGlobalError("documentGrid.error.document.quantity.notEnoughResources", false);
                return false;
            }
        }
        return true;

    }
}
