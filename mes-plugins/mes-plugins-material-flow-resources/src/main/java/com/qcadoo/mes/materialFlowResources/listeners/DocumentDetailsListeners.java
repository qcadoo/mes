package com.qcadoo.mes.materialFlowResources.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class DocumentDetailsListeners {

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Transactional
    public void createResourcesForDocuments(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        final Entity document = formComponent.getPersistedEntityWithIncludedFormValues();
        Entity savedDocument = document.getDataDefinition().save(document);
        if(!savedDocument.isValid()){
           formComponent.setEntity(savedDocument);
           return;
        }

        DocumentType documentType = DocumentType.parseString(document.getStringField(DocumentFields.TYPE));
        if (DocumentType.RECEIPT.equals(documentType) || DocumentType.INTERNAL_INBOUND.equals(documentType)) {
            resourceManagementService.createResourcesForReceiptDocuments(document);
        } else if (DocumentType.INTERNAL_OUTBOUND.equals(documentType) || DocumentType.RELEASE.equals(documentType)) {
            resourceManagementService.updateResourcesForReleaseDocuments(document);
        } else if (DocumentType.TRANSFER.equals(documentType)) {
            resourceManagementService.moveResourcesForTransferDocument(document);
        } else {
            throw new IllegalStateException("Unsupported document type");
        }

        document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
        savedDocument = document.getDataDefinition().save(document);
        if(!savedDocument.isValid()){
            savedDocument.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
        }
        formComponent.setEntity(savedDocument);
    }

    public void clearWarehouseFields(final ViewDefinitionState view, final ComponentState state, final String[] args){
        FieldComponent locationFrom = (FieldComponent) view.getComponentByReference("locationFrom");
        locationFrom.setFieldValue(null);
        locationFrom.requestComponentUpdateState();

        FieldComponent locationTo = (FieldComponent) view.getComponentByReference("locationTo");
        locationTo.setFieldValue(null);
        locationFrom.requestComponentUpdateState();
    }

    public void refreshView(final ViewDefinitionState view, final ComponentState state, final String[] args){
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        form.performEvent(view, "refresh");
    }
}
