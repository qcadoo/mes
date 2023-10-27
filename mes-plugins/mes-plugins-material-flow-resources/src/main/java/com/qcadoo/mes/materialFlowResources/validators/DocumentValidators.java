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

import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentValidators {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PositionValidators positionValidators;

    public boolean validate(final DataDefinition documentDD, final Entity document) {
        Long documentId = document.getId();

        if (documentId != null) {
            Entity documentFromDB = documentDD.get(documentId);

            if (!checkIfItIsReportGeneration(document, documentFromDB)
                    && DocumentState.ACCEPTED.getStringValue().equals(documentFromDB.getStringField(DocumentFields.STATE))) {
                document.addGlobalError("materialFlow.error.document.alreadyAccepted");

                return false;
            }
        }

        boolean hasWarehouses = hasWarehouses(documentDD, document);

        if (hasWarehouses) {
            hasDifferentWarehouses(documentDD, document);
        }

        validateWarehouseChanged(documentDD, document);

        return document.isValid();
    }

    public boolean validatesWith(final DataDefinition documentDD, final Entity document) {

        boolean createLinkedDocument = document.getBooleanField(DocumentFields.CREATE_LINKED_DOCUMENT);
        Entity locationPz = document.getBelongsToField(DocumentFields.LINKED_DOCUMENT_LOCATION);
        Entity locationFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);

        String type = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.RELEASE.getStringValue().equals(type) && createLinkedDocument && locationPz == null) {
            document.addError(documentDD.getField(DocumentFields.LINKED_DOCUMENT_LOCATION),
                    "qcadooView.validate.field.error.missing");

            return false;
        }
        if (DocumentType.RELEASE.getStringValue().equals(type) && createLinkedDocument && locationFrom != null
                && locationFrom.getId().equals(locationPz.getId())) {
            document.addError(documentDD.getField(DocumentFields.LINKED_DOCUMENT_LOCATION),
                    "materialFlowResources.document.error.linkedDocumentLocationEqualsFrom");

            return false;
        }

        return true;
    }

    private boolean checkIfItIsReportGeneration(final Entity document, final Entity documentFromDB) {
        return (((document.getDateField(DocumentFields.GENERATION_DATE) != null)
                && (documentFromDB.getDateField(DocumentFields.GENERATION_DATE) == null))
                || (StringUtils.isNotEmpty(document.getStringField(DocumentFields.FILE_NAME))
                        && StringUtils.isEmpty(documentFromDB.getStringField(DocumentFields.FILE_NAME))));
    }

    public boolean hasDifferentWarehouses(final DataDefinition documentDD, final Entity document) {
        DocumentType documentType = DocumentType.of(document);

        if (DocumentType.TRANSFER.equals(documentType)) {
            Entity locationFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            Entity locationTo = document.getBelongsToField(DocumentFields.LOCATION_TO);

            if (locationFrom == null || locationTo == null) {
                return true;
            }
            if (locationFrom.getId().equals(locationTo.getId())) {
                document.addGlobalError("materialFlow.error.document.warehouse.sameForTransfer");

                return false;
            }
        }
        return true;
    }

    public boolean hasWarehouses(final DataDefinition documentDD, final Entity document) {
        DocumentType documentType = DocumentType.of(document);

        if (DocumentType.RECEIPT.equals(documentType) || DocumentType.INTERNAL_INBOUND.equals(documentType)) {
            return hasWarehouse(documentDD, document, DocumentFields.LOCATION_TO);
        } else if (DocumentType.TRANSFER.equals(documentType)) {
            return hasWarehouse(documentDD, document, DocumentFields.LOCATION_FROM)
                    && hasWarehouse(documentDD, document, DocumentFields.LOCATION_TO);
        } else if (DocumentType.RELEASE.equals(documentType) || DocumentType.INTERNAL_OUTBOUND.equals(documentType)) {
            return hasWarehouse(documentDD, document, DocumentFields.LOCATION_FROM);
        } else {
            throw new IllegalStateException("Unknown document type.");
        }
    }

    private boolean hasWarehouse(final DataDefinition documentDD, final Entity document, String warehouseField) {
        Entity location = document.getBelongsToField(warehouseField);

        if (location == null) {
            document.addError(documentDD.getField(warehouseField), "materialFlow.error.document.warehouse.required");

            return false;
        }

        return true;
    }

    private boolean validateWarehouseChanged(final DataDefinition documentDD, final Entity document) {
        if (document.getId() == null || document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
            return true;
        }

        Entity documentFromDB = documentDD.get(document.getId());

        if (checkIfWarehouseHasChanged(documentFromDB, document, DocumentFields.LOCATION_FROM)
                || checkIfWarehouseHasChanged(documentFromDB, document, DocumentFields.LOCATION_TO)) {
            document.addGlobalError("materialFlow.document.validate.global.error.warehouseChanged");

            return false;
        }

        return true;
    }

    private boolean checkIfWarehouseHasChanged(final Entity oldDocument, final Entity newDocument, final String warehouseField) {
        Entity oldWarehouse = oldDocument.getBelongsToField(warehouseField);
        Entity newWarehouse = newDocument.getBelongsToField(warehouseField);

        if (oldWarehouse == null && newWarehouse == null) {
            return false;
        } else if (oldWarehouse != null && newWarehouse != null) {
            return oldWarehouse.getId().compareTo(newWarehouse.getId()) != 0;
        }

        return true;
    }
    
    private Map<Long, Entity> groupProductsInPositions(final List<Entity> positions) {
        Map<Long, Entity> groupedPositions = Maps.newHashMap();

        for (Entity position : positions) {
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);
            List<Entity> reservations = position.getHasManyField(PositionFields.RESERVATIONS);
            BigDecimal reservedQuantity = BigDecimal.ZERO;

            if (reservations != null && !reservations.isEmpty()) {
                Entity reservation = reservations.get(0);

                if (reservation.getId() != null) {
                    reservedQuantity = reservation.getDecimalField(ReservationFields.QUANTITY);
                }
            }
            if (groupedPositions.containsKey(product.getId())) {
                Entity existingPosition = groupedPositions.get(product.getId());
                BigDecimal oldQuantity = existingPosition.getDecimalField(PositionFields.QUANTITY);
                BigDecimal newQuantity = position.getDecimalField(PositionFields.QUANTITY);

                if (oldQuantity == null) {
                    existingPosition.setField(PositionFields.QUANTITY, newQuantity.subtract(reservedQuantity));
                } else {
                    if (newQuantity != null) {
                        existingPosition.setField(PositionFields.QUANTITY,
                                newQuantity.add(oldQuantity).subtract(reservedQuantity));
                    }
                }
            } else {
                Entity newPosition = dataDefinitionService
                        .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_POSITION)
                        .create();

                newPosition.setField(PositionFields.PRODUCT, product);
                newPosition.setField("id", position.getId());
                newPosition.setField(PositionFields.QUANTITY,
                        position.getDecimalField(PositionFields.QUANTITY).subtract(reservedQuantity));

                groupedPositions.put(product.getId(), newPosition);
            }
        }

        return groupedPositions;
    }

}
