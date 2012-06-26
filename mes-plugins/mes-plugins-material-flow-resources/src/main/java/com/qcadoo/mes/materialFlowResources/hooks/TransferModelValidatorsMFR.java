package com.qcadoo.mes.materialFlowResources.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelValidatorsMFR {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public boolean validateTransferResources(final DataDefinition transferDD, final Entity transfer) {
        boolean validate = true;

        String type = transfer.getStringField(TYPE);
        Entity locationFrom = transfer.getBelongsToField(LOCATION_FROM);
        Entity product = transfer.getBelongsToField(PRODUCT);
        BigDecimal quantity = transfer.getDecimalField(QUANTITY);

        if ((type != null) && !PRODUCTION.getStringValue().equals(type) && (locationFrom != null) && (product != null)
                && (quantity != null) && !materialFlowResourcesService.areResourcesSufficient(locationFrom, product, quantity)) {
            transfer.addError(transferDD.getField(QUANTITY),
                    "materialFlowResources.validate.global.error.resourcesArentSufficient");

            validate = false;
        }

        return validate;
    }
}
