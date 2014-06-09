package com.qcadoo.mes.materialFlowDocuments.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.materialFlowDocuments.constants.DocumentFields;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentState;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentType;
import com.qcadoo.mes.materialFlowDocuments.service.ResourceManagementService;
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

        document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
        Entity savedDocument = document.getDataDefinition().save(document);
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
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference("form");
        masterOrderForm.performEvent(view, "refresh");
    }
}
