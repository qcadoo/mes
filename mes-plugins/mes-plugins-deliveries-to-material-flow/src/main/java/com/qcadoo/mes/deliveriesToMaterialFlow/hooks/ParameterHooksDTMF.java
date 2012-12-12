package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.ParameterFieldsDTMF.LOCATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksDTMF {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public boolean checkIfLocationIsWarehouse(final DataDefinition parameterDD, final Entity parameter) {
        Entity location = parameter.getBelongsToField(LOCATION);

        if ((location != null) && !materialFlowResourcesService.isLocationIsWarehouse(location)) {
            parameter.addError(parameterDD.getField(LOCATION), "parameter.validate.global.error.locationIsNotWarehouse");

            return false;
        }

        return true;
    }

}
