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
package com.qcadoo.mes.materialFlowResources.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.hooks.DocumentDetailsHooks;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementServiceImpl.WarehouseAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;

@Service
public class DocumentDetailsListeners {

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private DocumentDetailsHooks documentDetailsHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_RESOURCE = "resource";

    private static final String L_BATCH = "batch";

    private static final String L_FORM = "form";

    public void createResourcesForDocuments(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {

        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        Entity document = formComponent.getPersistedEntityWithIncludedFormValues();
        document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
        Entity documentToCreateResourcesFor = documentDD.save(document);

        if (!documentToCreateResourcesFor.isValid()) {
            documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
            formComponent.setEntity(documentToCreateResourcesFor);
            return;
        }

        if (!validateResourceAttribute(document)) {
            formComponent.addMessage("materialFlow.error.position.batch.required", MessageType.FAILURE);
            documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
            formComponent.setEntity(documentToCreateResourcesFor);
            return;
        }

        if (!documentToCreateResourcesFor.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
            createResources(documentToCreateResourcesFor);
        } else {
            documentToCreateResourcesFor.setNotValid();
            formComponent.addMessage("materialFlow.document.validate.global.error.emptyPositions", MessageType.FAILURE);
            window.setActiveTab("positionsListTab");
        }

        if (!documentToCreateResourcesFor.isValid()) {
            Entity recentlySavedDocument = documentDD.get(document.getId());
            recentlySavedDocument.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
            documentDD.save(recentlySavedDocument);
            documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
        }
        formComponent.setEntity(documentToCreateResourcesFor);
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

    public void clearWarehouseFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent locationFrom = (FieldComponent) view.getComponentByReference("locationFrom");
        locationFrom.setFieldValue(null);
        locationFrom.requestComponentUpdateState();

        FieldComponent locationTo = (FieldComponent) view.getComponentByReference("locationTo");
        locationTo.setFieldValue(null);
        locationFrom.requestComponentUpdateState();

        showResourceLookupOrBatchInput(view, false, true);
    }

    public void refreshView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        form.performEvent(view, "refresh");
    }

    public void showAndSetRequiredForResourceLookup(final ViewDefinitionState view) {
        boolean visible = checkIfResourceLookupShouldBeVisible(view);
        showResourceLookupOrBatchInput(view, visible, false);
    }

    public void showAndSetRequiredForResourceLookup(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        showAndSetRequiredForResourceLookup(view);
        documentDetailsHooks.setCriteriaModifiersParameters(view);
    }

    private void showResourceLookupOrBatchInput(final ViewDefinitionState view, boolean visible, boolean shouldClear) {
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference("positions");
        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            FieldComponent resourceLookup = positionForm.findFieldComponentByName(L_RESOURCE);
            FieldComponent batchField = positionForm.findFieldComponentByName(L_BATCH);

            resourceLookup.setVisible(visible);
            resourceLookup.setRequired(visible);
            batchField.setVisible(!visible);
            if (shouldClear) {
                resourceLookup.setFieldValue(null);
            }
        }
    }

    private boolean checkIfResourceLookupShouldBeVisible(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        Entity locationFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        DocumentState state = DocumentState.of(document);
        if (locationFrom != null) {
            DocumentType type = DocumentType.of(document);
            String algorithm = locationFrom.getStringField(LocationFieldsMFR.ALGORITHM);
            return algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue())
                    && ((DocumentType.RELEASE.equals(type)) || (DocumentType.TRANSFER.equals(type)))
                    && DocumentState.DRAFT.equals(state);
        }
        return false;
    }

    private boolean validateResourceAttribute(Entity document) {
        DocumentType type = DocumentType.of(document);
        if (DocumentType.TRANSFER.equals(type) || DocumentType.RELEASE.equals(type)) {
            Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            String algorithm = warehouseFrom.getStringField(LocationFieldsMFR.ALGORITHM);
            boolean result = true;
            for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
                boolean resultForPosition = (algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue()) && position
                        .getField(PositionFields.RESOURCE) != null)
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
}
