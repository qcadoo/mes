package com.qcadoo.mes.materialFlowResources.validators;

import static com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR.CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DocumentPositionParametersValidators {

    public final boolean checkIfChangeDateWhenTransferToWarehouseTypeIsSelected(final DataDefinition dataDefinition,
                                                                                final Entity documentPositionParameters) {
        String changeDateWhenTransferToWarehouseType = documentPositionParameters.getStringField(CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE);

        if (changeDateWhenTransferToWarehouseType == null) {
            documentPositionParameters.addError(dataDefinition.getField(CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE),
                    "materialFlowResources.documentPositionParameters.message.changeDateWhenTransferToWarehouseTypeIsNotSelected");

            return false;
        }

        return true;
    }
}
