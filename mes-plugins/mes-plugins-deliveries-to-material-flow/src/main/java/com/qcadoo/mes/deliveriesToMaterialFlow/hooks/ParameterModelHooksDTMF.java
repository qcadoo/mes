package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.LocationFieldsDTMF.LOCATION;
import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.LocationFieldsDTMF.TYPE;
import static com.qcadoo.mes.materialFlowResources.constants.LocationTypeMFR.WAREHOUSE;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterModelHooksDTMF {

    public boolean checkIfLocationIsWarehouse(final DataDefinition parameterDD, final Entity parameter) {
        Entity location = parameter.getBelongsToField(LOCATION);

        if (location != null) {
            String locationType = location.getStringField(TYPE);

            if (!WAREHOUSE.getStringValue().equals(locationType)) {
                parameter.addError(parameterDD.getField("location"), "parameter.validate.global.error.locationIsNotWarehouse");
                return false;
            }
        }
        return true;
    }
}
