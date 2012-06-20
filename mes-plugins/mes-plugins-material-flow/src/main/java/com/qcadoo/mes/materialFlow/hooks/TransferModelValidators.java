package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.MaterialFlowTransferService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelValidators {

    @Autowired
    private MaterialFlowTransferService materialFlowTransferService;

    public boolean validateTransfer(final DataDefinition transferDD, final Entity transfer) {
        boolean validate = true;

        String type = transfer.getStringField(TYPE);
        Date time = (Date) transfer.getField(TIME);
        Entity stockAreasFrom = transfer.getBelongsToField(STOCK_AREAS_FROM);
        Entity stockAreasTo = transfer.getBelongsToField(STOCK_AREAS_TO);
        Entity product = transfer.getBelongsToField(PRODUCT);
        BigDecimal quantity = transfer.getDecimalField(QUANTITY);

        if (type == null) {
            transfer.addError(transferDD.getField(TYPE), "materialFlow.validate.global.error.fillType");
            validate = false;
        }
        if (time == null) {
            transfer.addError(transferDD.getField(TIME), "materialFlow.validate.global.error.fillDate");
            validate = false;
        }
        if (stockAreasFrom == null && stockAreasTo == null) {
            transfer.addError(transferDD.getField(STOCK_AREAS_FROM),
                    "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
            transfer.addError(transferDD.getField(STOCK_AREAS_TO), "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
            validate = false;
        }
        if (!materialFlowTransferService.isTransferValidAndAreResourcesSufficient(stockAreasFrom, product, quantity)) {
            transfer.addError(transferDD.getField(QUANTITY), "materialFlow.validate.global.error.resourcesArentSufficient");
            validate = false;
        }

        return validate;
    }
}
