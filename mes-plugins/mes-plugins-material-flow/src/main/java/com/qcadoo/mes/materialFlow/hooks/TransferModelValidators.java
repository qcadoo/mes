package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelValidators {

    public boolean validateTransfer(final DataDefinition transferDD, final Entity transfer) {
        boolean validate = true;

        String type = transfer.getStringField(TYPE);
        Date time = (Date) transfer.getField(TIME);
        Entity locationFrom = transfer.getBelongsToField(LOCATION_FROM);
        Entity locationTo = transfer.getBelongsToField(LOCATION_TO);

        if (type == null) {
            transfer.addError(transferDD.getField(TYPE), "materialFlow.validate.global.error.fillType");

            validate = false;
        }

        if (time == null) {
            transfer.addError(transferDD.getField(TIME), "materialFlow.validate.global.error.fillDate");

            validate = false;
        }

        if (locationFrom == null && locationTo == null) {
            transfer.addError(transferDD.getField(LOCATION_FROM), "materialFlow.validate.global.error.fillAtLeastOneLocation");
            transfer.addError(transferDD.getField(LOCATION_TO), "materialFlow.validate.global.error.fillAtLeastOneLocation");

            validate = false;
        }

        return validate;
    }

}
