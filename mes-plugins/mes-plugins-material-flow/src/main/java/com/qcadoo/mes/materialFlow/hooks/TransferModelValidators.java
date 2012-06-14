package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.TRANSPORT;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.MaterialFlowResourceService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelValidators {

    @Autowired
    private MaterialFlowResourceService materialFlowResourceService;

    public boolean validateTransfer(final DataDefinition transferDD, final Entity transfer) {
        boolean validate = true;

        Entity stockAreasFrom = transfer.getBelongsToField(STOCK_AREAS_FROM);
        Entity stockAreasTo = transfer.getBelongsToField(STOCK_AREAS_TO);
        Entity product = transfer.getBelongsToField(PRODUCT);
        BigDecimal quantity = transfer.getDecimalField(QUANTITY);
        Date date = (Date) transfer.getField(TIME);
        String type = transfer.getStringField(TYPE);

        if (stockAreasFrom == null && stockAreasTo == null) {
            transfer.addError(transferDD.getField(STOCK_AREAS_FROM),
                    "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
            transfer.addError(transferDD.getField(STOCK_AREAS_TO), "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
            validate = false;
        }
        if (type == null) {
            transfer.addError(transferDD.getField(TYPE), "materialFlow.validate.global.error.fillType");
            validate = false;
        }
        if (date == null) {
            transfer.addError(transferDD.getField(TIME), "materialFlow.validate.global.error.fillDate");
            validate = false;
        }
        if ((CONSUMPTION.getStringValue().equals(type) || TRANSPORT.getStringValue().equals(type))
                && !materialFlowResourceService.areResourcesSufficient(stockAreasFrom, product, quantity)) {
            transfer.addError(transferDD.getField(QUANTITY), "materialFlow.validate.global.error.resourcesArentSufficient");
            validate = false;
        }

        return validate;
    }
}
