package com.qcadoo.mes.deliveriesToMaterialFlow;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterModelHooksDTMF {

    public boolean checkIfLocationIsWarehouse(final DataDefinition parameterDD, final Entity parameter) {
        if (!parameter.getBelongsToField("location").getStringField("type").contains("02warehouse")) {
            parameter.addError(parameterDD.getField("location"), "parameter.validate.global.error.locationIsNotWarehouse");
            return false;
        }
        return true;
    }
}
