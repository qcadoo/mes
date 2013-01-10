package com.qcadoo.mes.materialFlowResources.hooks;

import static com.qcadoo.mes.materialFlowResources.constants.ChangeDateWhenTransferToWarehouseType.NEVER;
import static com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR.CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksMFR {

    public final boolean checkIfChangeDateWhenTransferToWarehouseTypeIsSelected(final DataDefinition parameterDD,
            final Entity parameter) {
        String changeDateWhenTransferToWarehouseType = parameter.getStringField(CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE);

        if (changeDateWhenTransferToWarehouseType == null) {
            parameter.addError(parameterDD.getField(CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE),
                    "basic.parameter.message.changeDateWhenTransferToWarehouseTypeIsNotSelected");

            return false;
        }

        return true;
    }

    public void setChangeDateWhenTransferToWarehouseTypeDefaultValue(final DataDefinition parameterDD, final Entity parameter) {
        String changeDateWhenTransferToWarehouseType = parameter.getStringField(CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE);

        if (changeDateWhenTransferToWarehouseType == null) {
            parameter.setField(CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE, NEVER.getStringValue());
        }
    }

}
