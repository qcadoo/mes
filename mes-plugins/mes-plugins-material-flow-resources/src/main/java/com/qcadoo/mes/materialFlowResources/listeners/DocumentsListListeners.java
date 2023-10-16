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

import java.util.List;

import com.qcadoo.mes.materialFlowResources.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.OrdersGroupIssuedMaterialFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class DocumentsListListeners {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsListListeners.class);

    public static final String REALIZED = "05realized";

    public static final String MOBILE_WMS = "mobileWMS";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private ReceiptDocumentForReleaseHelper receiptDocumentForReleaseHelper;

    @Autowired
    private DocumentErrorsLogger documentErrorsLogger;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DocumentStateChangeService documentStateChangeService;

    @Autowired
    private List<AfterDocumentAcceptListener> afterDocumentAcceptListeners;


    public void createResourcesForDocuments(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> documentsFromDB = Lists.newArrayList();

        boolean allAccepted = getDocumentsToAccept(documentDD, gridComponent, documentsFromDB);

        if (!allAccepted) {
            gridComponent.addMessage("materialFlow.info.document.acceptInfo", ComponentState.MessageType.INFO);
        }

        if (!documentsFromDB.isEmpty()) {
            documentService.setAcceptationInProgress(documentsFromDB, true);
            try {
                createResourcesForDocuments(view, gridComponent, documentDD, documentsFromDB);
            } catch (Exception e) {
                gridComponent.addMessage("materialFlow.error.document.acceptError", ComponentState.MessageType.FAILURE);
                LOG.error("Error in createResourcesForDocuments ", e);
                throw new IllegalStateException(e.getMessage(), e);
            } finally {
                documentService.setAcceptationInProgress(documentsFromDB, false);
            }
        }
    }

    private boolean getDocumentsToAccept(DataDefinition documentDD, GridComponent gridComponent, List<Entity> documentsFromDB) {
        boolean allAccepted = true;
        for (Long documentId : gridComponent.getSelectedEntitiesIds()) {
            Entity documentFromDB = documentDD.get(documentId);

            if (documentFromDB != null) {
                if (DocumentState.ACCEPTED.getStringValue().equals(documentFromDB.getStringField(DocumentFields.STATE))) {
                    allAccepted = false;
                    continue;
                }

                if (documentService.getAcceptationInProgress(documentId)) {
                    allAccepted = false;
                    continue;
                }

                if (pluginManager.isPluginEnabled(MOBILE_WMS) && documentFromDB.getBooleanField(DocumentFields.WMS)
                        && !REALIZED.equals(documentFromDB.getStringField(DocumentFields.STATE_IN_WMS))) {
                    allAccepted = false;
                    continue;
                }

                documentsFromDB.add(documentFromDB);
            }
        }
        return allAccepted;
    }

    @Transactional
    public void createResourcesForDocuments(final ViewDefinitionState view, final GridComponent gridComponent,
            final DataDefinition documentDD, List<Entity> documents) {
        for (Entity document : documents) {
            document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
            document.setField(DocumentFields.ACCEPTATION_IN_PROGRESS, false);

            document = documentDD.save(document);

            if (!document.isValid()) {
                documentStateChangeService.buildFailureStateChange(document.getId());
                continue;
            }

            if (!document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
                String blockedResources = documentService.getBlockedResources(document);
                if (blockedResources == null) {
                    resourceManagementService.createResources(document);
                } else {
                    document.setNotValid();

                    gridComponent.addMessage("materialFlow.document.validate.global.error.positionsBlockedForQualityControl",
                            ComponentState.MessageType.FAILURE, document.getStringField(DocumentFields.NUMBER), blockedResources);
                }
            } else {
                document.setNotValid();

                gridComponent.addMessage("materialFlow.document.validate.global.error.emptyPositions",
                        ComponentState.MessageType.FAILURE, document.getStringField(DocumentFields.NUMBER));
            }

            if (!document.isValid()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

                documentErrorsLogger.saveResourceStockLackErrorsToSystemLogs(document);

                documentStateChangeService.buildFailureStateChangeAfterRollback(document.getId());

                for (AfterDocumentAcceptListener afterDocumentAcceptListener : afterDocumentAcceptListeners) {
                    afterDocumentAcceptListener.run(document);
                }

                document.getGlobalErrors().forEach(gridComponent::addMessage);
                document.getErrors().values().forEach(gridComponent::addMessage);
            } else {
                receiptDocumentForReleaseHelper.tryBuildConnectedDocument(document, view);

                documentService.updateOrdersGroupIssuedMaterials(
                        document.getBelongsToField(OrdersGroupIssuedMaterialFields.ORDERS_GROUP), null);
            }
        }
    }

}
