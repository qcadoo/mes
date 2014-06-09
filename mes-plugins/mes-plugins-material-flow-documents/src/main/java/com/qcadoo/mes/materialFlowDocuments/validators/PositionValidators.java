package com.qcadoo.mes.materialFlowDocuments.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowDocuments.constants.DocumentFields;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentType;
import com.qcadoo.mes.materialFlowDocuments.constants.LocationFieldsMFD;
import com.qcadoo.mes.materialFlowDocuments.constants.PositionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PositionValidators {

    public boolean checkAttributesRequirement(final DataDefinition dataDefinition, final Entity position) {

        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);

        String documentType = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.RECEIPT.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_INBOUND.getStringValue().equals(documentType)) {
            Entity warehouseTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
            return validatePositionAttributes(dataDefinition, position,
                    warehouseTo.getBooleanField(LocationFieldsMFD.REQUIRE_PRICE),
                    warehouseTo.getBooleanField(LocationFieldsMFD.REQUIRE_BATCH),
                    warehouseTo.getBooleanField(LocationFieldsMFD.REQUIRE_PRODUCTION_DATE),
                    warehouseTo.getBooleanField(LocationFieldsMFD.REQUIRE_EXPIRATION_DATE));
        }

        return true;
    }

    private boolean validatePositionAttributes(DataDefinition dataDefinition, Entity position, boolean requirePrice,
            boolean requireBatch, boolean requireProductionDate, boolean requireExpirationDate) {

        boolean result = true;
        if (requirePrice && position.getField(PositionFields.PRICE) == null) {
            position.addError(dataDefinition.getField(PositionFields.PRICE), "materialFlow.error.position.price.required");
            result = false;
        }
        if (requireBatch && position.getField(PositionFields.BATCH) == null) {
            position.addError(dataDefinition.getField(PositionFields.BATCH), "materialFlow.error.position.batch.required");
            result = false;
        }
        if (requireProductionDate && position.getField(PositionFields.PRODUCTION_DATE) == null) {
            position.addError(dataDefinition.getField(PositionFields.PRODUCTION_DATE),
                    "materialFlow.error.position.productionDate.required");
            result = false;
        }

        if (requireExpirationDate && position.getField(PositionFields.EXPIRATION_DATE) == null) {
            position.addError(dataDefinition.getField(PositionFields.EXPIRATION_DATE),
                    "materialFlow.error.position.expirationDate.required");
            result = false;
        }

        return result;
    }
}
