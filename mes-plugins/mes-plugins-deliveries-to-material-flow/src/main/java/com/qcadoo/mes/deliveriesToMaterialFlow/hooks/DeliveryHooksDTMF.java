package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveryFieldsDTMF.LOCATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveryHooksDTMF {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void setLocationDefaultValue(final DataDefinition deliveryDD, final Entity delivery) {
        Entity location = delivery.getBelongsToField(LOCATION);

        if (location == null) {
            delivery.setField(LOCATION, parameterService.getParameter().getBelongsToField(LOCATION));
        }
    }

    public boolean checkIfLocationIsWarehouse(final DataDefinition deliveryDD, final Entity delivery) {
        Entity location = delivery.getBelongsToField(LOCATION);

        if ((location != null) && !materialFlowResourcesService.isLocationIsWarehouse(location)) {
            delivery.addError(deliveryDD.getField(LOCATION), "delivery.validate.global.error.locationIsNotWarehouse");

            return false;
        }

        return true;
    }

}
