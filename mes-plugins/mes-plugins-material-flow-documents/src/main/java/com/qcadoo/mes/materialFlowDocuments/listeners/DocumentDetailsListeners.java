package com.qcadoo.mes.materialFlowDocuments.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowDocuments.constants.DocumentFields;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentType;
import com.qcadoo.mes.materialFlowDocuments.service.ResourceManagementService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class DocumentDetailsListeners {

    @Autowired
    private ResourceManagementService resourceManagementService;

    public void createResourcesForReceiptDocuments(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        final Entity document = formComponent.getPersistedEntityWithIncludedFormValues();

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
    }
}
