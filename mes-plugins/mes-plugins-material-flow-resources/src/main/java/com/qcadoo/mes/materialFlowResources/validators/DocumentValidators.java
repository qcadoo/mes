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

import com.beust.jcommander.internal.Sets;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.exceptions.InvalidResourceException;
import com.qcadoo.mes.materialFlowResources.service.DocumentService;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class DocumentValidators {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    public boolean validate(final DataDefinition documentDD, final Entity document) {
        Long documentId = document.getId();

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
        if (DocumentType.RELEASE.getStringValue().equals(type) && createLinkedDocument && Objects.isNull(locationPz)) {
            document.addError(documentDD.getField(DocumentFields.LINKED_DOCUMENT_LOCATION),
                    "qcadooView.validate.field.error.missing");

            return false;
        }
        if (DocumentType.RELEASE.getStringValue().equals(type) && createLinkedDocument && Objects.nonNull(locationFrom)
                && locationFrom.getId().equals(locationPz.getId())) {
            document.addError(documentDD.getField(DocumentFields.LINKED_DOCUMENT_LOCATION),
                    "materialFlowResources.document.error.linkedDocumentLocationEqualsFrom");

            return false;
        }

        return true;
    }

    private boolean checkIfItIsReportGeneration(final Entity document, final Entity documentFromDB) {
        return ((Objects.nonNull(document.getDateField(DocumentFields.GENERATION_DATE))
                && Objects.isNull(documentFromDB.getDateField(DocumentFields.GENERATION_DATE)))
                || (StringUtils.isNotEmpty(document.getStringField(DocumentFields.FILE_NAME))
                && StringUtils.isEmpty(documentFromDB.getStringField(DocumentFields.FILE_NAME))));
    }

    public boolean hasDifferentWarehouses(final DataDefinition documentDD, final Entity document) {
        DocumentType documentType = DocumentType.of(document);

        if (DocumentType.TRANSFER.equals(documentType)) {
            Entity locationFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            Entity locationTo = document.getBelongsToField(DocumentFields.LOCATION_TO);

            if (Objects.isNull(locationFrom) || Objects.isNull(locationTo)) {
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

        if (Objects.isNull(location)) {
            document.addError(documentDD.getField(warehouseField), "materialFlow.error.document.warehouse.required");

            return false;
        }

        return true;
    }

    private boolean validateWarehouseChanged(final DataDefinition documentDD, final Entity document) {
        if (Objects.isNull(document.getId()) || document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
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

        if (Objects.isNull(oldWarehouse) && Objects.isNull(newWarehouse)) {
            return false;
        } else if (Objects.nonNull(oldWarehouse) && Objects.nonNull(newWarehouse)) {
            return oldWarehouse.getId().compareTo(newWarehouse.getId()) != 0;
        }

        return true;
    }

    public void validatePositionsAndCreateResources(final ComponentState documentForm, final Entity document) {
        if (!document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
            if (validatePositions(document)) {
                String blockedResources = documentService.getBlockedResources(document);

                if (Objects.isNull(blockedResources)) {
                    try {
                        resourceManagementService.createResources(document);
                    } catch (InvalidResourceException ire) {
                        document.setNotValid();

                        String productNumber = ire.getEntity().getBelongsToField(ResourceFields.PRODUCT)
                                .getStringField(ProductFields.NUMBER);
                        String resourceNumber = ire.getEntity().getStringField(ResourceFields.NUMBER);

                        ErrorMessage batchError = ire.getEntity().getError(ResourceFields.BATCH);

                        if (Objects.nonNull(batchError) && "materialFlow.error.position.batch.required".equals(batchError.getMessage())) {
                            documentForm.addMessage("materialFlow.document.validate.global.error.invalidResource.batchRequired",
                                    ComponentState.MessageType.FAILURE, false, productNumber);
                        } else {
                            documentForm.addMessage("materialFlow.document.validate.global.error.invalidResource",
                                    ComponentState.MessageType.FAILURE, false, resourceNumber, productNumber);
                        }
                    }
                } else {
                    document.setNotValid();

                    documentForm.addMessage("materialFlow.document.validate.global.error.positionsBlockedForQualityControl",
                            ComponentState.MessageType.FAILURE, document.getStringField(DocumentFields.NUMBER), blockedResources);
                }
            } else {
                document.setNotValid();

                documentForm.addMessage("qcadooView.validate.global.error.custom", ComponentState.MessageType.FAILURE);
            }
        } else {
            document.setNotValid();

            documentForm.addMessage("materialFlow.document.validate.global.error.emptyPositions", ComponentState.MessageType.FAILURE,
                    document.getStringField(DocumentFields.NUMBER));
        }
    }

    private boolean validatePositions(final Entity document) {
        if (Objects.isNull(document.getId()) || document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
            return true;
        }

        boolean isValid = true;

        String type = document.getStringField(DocumentFields.TYPE);

        if (DocumentType.RECEIPT.getStringValue().equals(type) || DocumentType.INTERNAL_INBOUND.getStringValue().equals(type)) {
            String number = document.getStringField(DocumentFields.NUMBER);
            List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);

            Set<String> missingStorageLocations = Sets.newHashSet();
            Set<String> missingPalletNumbers = Sets.newHashSet();
            Set<String> existsMorePallets = Sets.newHashSet();

            positions.forEach(position -> {
                Integer positionNumber = position.getIntegerField(PositionFields.NUMBER);
                Entity storageLocation = position.getBelongsToField(PositionFields.STORAGE_LOCATION);
                Entity palletNumber = position.getBelongsToField(PositionFields.PALLET_NUMBER);

                if (Objects.isNull(storageLocation) && Objects.nonNull(palletNumber)) {
                    missingStorageLocations.add(positionNumber.toString());
                } else {
                    if (Objects.nonNull(storageLocation)) {
                        String storageLocationNumber = storageLocation.getStringField(StorageLocationFields.NUMBER);
                        boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

                        if (placeStorageLocation) {
                            if (Objects.isNull(palletNumber)) {
                                missingPalletNumbers.add(positionNumber.toString());
                            } else {
                                String palletNumberNumber = palletNumber.getStringField(PalletNumberFields.NUMBER);

                                if (palletValidatorService.tooManyPalletsInStorageLocationAndPositions(storageLocationNumber, palletNumberNumber, position.getId())) {
                                    existsMorePallets.add(positionNumber.toString());
                                }
                            }
                        }
                    }
                }
            });

            if (!missingStorageLocations.isEmpty()) {
                document.addGlobalError("materialFlow.document.validate.global.error.position.storageLocationRequired", number, String.join(", ", missingStorageLocations));

                isValid = false;
            }
            if (!missingPalletNumbers.isEmpty()) {
                document.addGlobalError("materialFlow.document.validate.global.error.position.palletNumberRequired", number, String.join(", ", missingPalletNumbers));

                isValid = false;
            }
            if (!existsMorePallets.isEmpty()) {
                document.addGlobalError("materialFlow.document.validate.global.error.position.morePalletsExists", number, String.join(", ", existsMorePallets));

                isValid = false;
            }
        }

        return isValid;
    }

}
