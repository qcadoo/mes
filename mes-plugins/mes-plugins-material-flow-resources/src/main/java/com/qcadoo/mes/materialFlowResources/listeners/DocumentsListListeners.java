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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.service.ReceiptDocumentForReleaseHelper;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class DocumentsListListeners {

    private static final String L_GRID = "grid";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private ReceiptDocumentForReleaseHelper receiptDocumentForReleaseHelper;

    @Autowired
    private DocumentErrorsLogger documentErrorsLogger;

    @Transactional
    public void createResourcesForDocuments(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        GridComponent gridComponent = (GridComponent) view.getComponentByReference(L_GRID);

        for (Long documentId : gridComponent.getSelectedEntitiesIds()) {
            Entity document = documentDD.get(documentId);
            if (!DocumentState.DRAFT.getStringValue().equals(document.getStringField(DocumentFields.STATE))) {
                continue;
            }

            document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
            document = documentDD.save(document);

            if (!document.isValid()) {
                continue;
            }

            if (!document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
                resourceManagementService.createResources(document);
            } else {
                document.setNotValid();
                gridComponent.addMessage("materialFlow.document.validate.global.error.emptyPositions", ComponentState.MessageType.FAILURE);
            }

            if (!document.isValid()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                documentErrorsLogger.saveResourceStockLackErrorsToSystemLogs(document);

                document.getGlobalErrors().forEach(gridComponent::addMessage);
                document.getErrors().values().forEach(gridComponent::addMessage);
            } else {
                if (receiptDocumentForReleaseHelper.buildConnectedPZDocument(document)) {
                    receiptDocumentForReleaseHelper.tryBuildPz(document, view);
                }
            }
        }
    }
}
