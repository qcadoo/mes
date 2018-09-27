package com.qcadoo.mes.productFlowThruDivision.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.DivisionFieldsPFTD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DivisionValidatorsPFTD {

    public boolean checkIfWarehousesAreDifferent(final DataDefinition dataDefinition, final Entity division) {

        Entity componentsLocation = division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_LOCATION);
        Entity componentsOutLocation = division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);

        if (componentsLocation != null && componentsOutLocation != null
                && componentsLocation.getId().equals(componentsOutLocation.getId())) {
            division.addGlobalError("technologies.technology.error.componentsLocationsAreSame", false);
            return false;
        }
        return true;
    }
}
