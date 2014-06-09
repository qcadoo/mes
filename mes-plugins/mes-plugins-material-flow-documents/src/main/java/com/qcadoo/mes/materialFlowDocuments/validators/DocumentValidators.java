package com.qcadoo.mes.materialFlowDocuments.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowDocuments.constants.DocumentFields;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DocumentValidators {

    public boolean hasWarehouses(final DataDefinition dataDefinition, final Entity entity) {

        DocumentType documentType = DocumentType.parseString(entity.getStringField(DocumentFields.TYPE));
        if (DocumentType.RECEIPT.equals(documentType) || DocumentType.INTERNAL_INBOUND.equals(documentType)) {
            return hasWarehouse(dataDefinition, entity, DocumentFields.LOCATION_TO);
        } else if (DocumentType.TRANSFER.equals(documentType)) {
            return hasWarehouse(dataDefinition, entity, DocumentFields.LOCATION_FROM)
                    && hasWarehouse(dataDefinition, entity, DocumentFields.LOCATION_TO);
        } else if (DocumentType.RELEASE.equals(documentType) || DocumentType.INTERNAL_OUTBOUND.equals(documentType)) {
            return hasWarehouse(dataDefinition, entity, DocumentFields.LOCATION_FROM);
        } else {
            throw new IllegalStateException("Unknown document type.");
        }
    }

    private boolean hasWarehouse(final DataDefinition dataDefinition, final Entity entity, String warehouseField) {
        if (entity.getField(warehouseField) != null) {
            return true;
        }
        entity.addError(dataDefinition.getField(warehouseField), "materialFlow.error.document.warehouse.required");
        return false;
    }
}
