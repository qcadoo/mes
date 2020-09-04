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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.qcadoo.view.constants.QcadooViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentsListListeners {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsListListeners.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private ReceiptDocumentForReleaseHelper receiptDocumentForReleaseHelper;

    @Autowired
    private DocumentErrorsLogger documentErrorsLogger;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void createResourcesForDocuments(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> documentsFromDB = Lists.newArrayList();

        for (Long documentId : gridComponent.getSelectedEntitiesIds()) {
            Entity documentFromDB = documentDD.get(documentId);

            if (documentFromDB != null) {
                if (DocumentState.ACCEPTED.getStringValue().equals(documentFromDB.getStringField(DocumentFields.STATE))) {
                    continue;
                }

                if (getAcceptationInProgress(documentId)) {
                    // if (documentFromDB.getBooleanField(DocumentFields.ACCEPTATION_IN_PROGRESS)) {
                    continue;
                }

                documentsFromDB.add(documentFromDB);
            }
        }

        if (!documentsFromDB.isEmpty()) {
            setAcceptationInProgress(documentsFromDB, true);
            try {
                createResourcesForDocuments(view, gridComponent, documentDD, documentsFromDB);
            } catch (Exception e) {
                gridComponent.addMessage("materialFlow.error.document.acceptError", ComponentState.MessageType.FAILURE);
                LOG.error("Error in createResourcesForDocuments ", e);
                throw new IllegalStateException(e.getMessage(), e);
            } finally {
                setAcceptationInProgress(documentsFromDB, false);
            }
        }
    }

    private boolean getAcceptationInProgress(final Long documentId) {
        String sql = "SELECT acceptationinprogress FROM materialflowresources_document WHERE id = :id;";
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("id", documentId);
        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);

        return jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
    }

    private void setAcceptationInProgress(final List<Entity> documents, final boolean acceptationInProgress) {
        String sql = "UPDATE materialflowresources_document SET acceptationinprogress = :acceptationinprogress WHERE id IN (:ids);";

        List<Long> ids = documents.stream().map(document -> document.getId()).collect(Collectors.toList());
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("acceptationinprogress", acceptationInProgress);

        parameters.put("ids", ids);

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);
        LOG.info("DOCUMENT SET ACCEPTATION IN PROGRESS = " + acceptationInProgress + " ids ="
                + ids.stream().map(Object::toString).collect(Collectors.joining(", ")));
        jdbcTemplate.update(sql, namedParameters);
    }

    @Transactional
    public void createResourcesForDocuments(final ViewDefinitionState view, final GridComponent gridComponent,
            final DataDefinition documentDD, List<Entity> documents) {
        for (Entity document : documents) {
            document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
            document.setField(DocumentFields.ACCEPTATION_IN_PROGRESS, false);

            document = documentDD.save(document);

            if (!document.isValid()) {
                continue;
            }

            if (!document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
                resourceManagementService.createResources(document);
            } else {
                document.setNotValid();

                gridComponent.addMessage("materialFlow.document.validate.global.error.emptyPositions",
                        ComponentState.MessageType.FAILURE);
            }

            if (!document.isValid()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

                documentErrorsLogger.saveResourceStockLackErrorsToSystemLogs(document);

                document.getGlobalErrors().forEach(gridComponent::addMessage);
                document.getErrors().values().forEach(gridComponent::addMessage);
            } else {
                if (receiptDocumentForReleaseHelper.buildConnectedDocument(document)) {
                    receiptDocumentForReleaseHelper.tryBuildConnectedDocument(document, view);
                }
            }
        }
    }

}
