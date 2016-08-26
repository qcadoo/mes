/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited Project: Qcadoo MES Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseAlgorithm;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class DocumentsListListeners {

    private static final String L_GRID = "grid";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceManagementService resourceManagementService;

    public void printDispositionOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity documentPositionParameters = parameterService.getParameter().getBelongsToField("documentPositionParameters");
        boolean acceptanceOfDocumentBeforePrinting = documentPositionParameters.getBooleanField("acceptanceOfDocumentBeforePrinting");
        Set<Long> invalidEntities = new HashSet<>();
        if (acceptanceOfDocumentBeforePrinting) {
            invalidEntities = createResourcesForDocuments(view);
        }
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        Set<Long> selectedEntitiesIds = grid.getSelectedEntitiesIds();

        if (invalidEntities.isEmpty()) {
            view.redirectTo("/materialFlowResources/dispositionOrder." + args[0] + "?id=" + selectedEntitiesIds.stream().map(String::valueOf).collect(Collectors.joining(",")), true, false);
        }
    }

    private Set<Long> createResourcesForDocuments(final ViewDefinitionState view) {
        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        GridComponent gridComponent = (GridComponent) view.getComponentByReference(L_GRID);
        Set<Long> selectedEntitiesIds = gridComponent.getSelectedEntitiesIds();
        Set<Long> invalidEntities = new HashSet<>();

        for (Long documentId : selectedEntitiesIds) {
            Entity document = documentDD.get(documentId);
            String documentState = document.getStringField(DocumentFields.STATE);
            if (!DocumentState.DRAFT.getStringValue().equals(documentState)) {
                continue;
            }

            document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
            Entity documentToCreateResourcesFor = documentDD.save(document);

            if (!documentToCreateResourcesFor.isValid()) {
                documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
                invalidEntities.add(documentId);
                continue;
            }

            if (!validateResourceAttribute(document)) {
                gridComponent.addMessage("materialFlow.error.position.batch.required", ComponentState.MessageType.FAILURE);
                documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
                invalidEntities.add(documentId);
                continue;
            }

            if (!documentToCreateResourcesFor.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
                createResources(documentToCreateResourcesFor);

            } else {
                documentToCreateResourcesFor.setNotValid();
                gridComponent.addMessage("materialFlow.document.validate.global.error.emptyPositions", ComponentState.MessageType.FAILURE);
                invalidEntities.add(documentId);
            }

            if (!documentToCreateResourcesFor.isValid()) {
                Entity recentlySavedDocument = documentDD.get(document.getId());
                recentlySavedDocument.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
                documentDD.save(recentlySavedDocument);
                documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
                
                documentToCreateResourcesFor.getGlobalErrors().forEach(error -> {
                    gridComponent.addMessage(error);
                });
                documentToCreateResourcesFor.getErrors().values().forEach(error -> {
                    gridComponent.addMessage(error);
                });
                
                invalidEntities.add(documentId);
            } 
            documentToCreateResourcesFor = documentToCreateResourcesFor.getDataDefinition().save(documentToCreateResourcesFor);
            updatePositions(documentToCreateResourcesFor);
        }

        return invalidEntities;
    }

    private boolean validateResourceAttribute(Entity document) {
        DocumentType type = DocumentType.of(document);
        if (DocumentType.TRANSFER.equals(type) || DocumentType.RELEASE.equals(type)) {
            Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            String algorithm = warehouseFrom.getStringField(LocationFieldsMFR.ALGORITHM);
            boolean result = true;
            for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
                boolean resultForPosition = (algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue())
                        && position.getField(PositionFields.RESOURCE) != null)
                        || !algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue());
                if (!resultForPosition) {
                    result = false;
                    position.addError(position.getDataDefinition().getField(PositionFields.RESOURCE),
                            "materialFlow.error.position.batch.required");
                }
            }
            return result;
        }
        return true;
    }

    private void updatePositions(Entity document) {
        String query = "UPDATE materialflowresources_position "
                + "SET type = (SELECT type FROM materialflowresources_document WHERE id=:document_id), state = (SELECT state FROM materialflowresources_document WHERE id=:document_id) "
                + "WHERE document_id = :document_id ";

        Map<String, Object> params = new HashMap<>();
        params.put("document_id", document.getId());
        jdbcTemplate.update(query, params);
    }

    @Transactional
    public void createResources(Entity documentToCreateResourcesFor) {
        DocumentType documentType = DocumentType.of(documentToCreateResourcesFor);
        if (DocumentType.RECEIPT.equals(documentType) || DocumentType.INTERNAL_INBOUND.equals(documentType)) {
            resourceManagementService.createResourcesForReceiptDocuments(documentToCreateResourcesFor);
        } else if (DocumentType.INTERNAL_OUTBOUND.equals(documentType) || DocumentType.RELEASE.equals(documentType)) {
            resourceManagementService.updateResourcesForReleaseDocuments(documentToCreateResourcesFor);
        } else if (DocumentType.TRANSFER.equals(documentType)) {
            resourceManagementService.moveResourcesForTransferDocument(documentToCreateResourcesFor);
        } else {
            throw new IllegalStateException("Unsupported document type");
        }
        if (!documentToCreateResourcesFor.isValid()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

}
