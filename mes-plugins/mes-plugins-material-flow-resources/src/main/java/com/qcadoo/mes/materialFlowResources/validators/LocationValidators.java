package com.qcadoo.mes.materialFlowResources.validators;

import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.LocationType;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class LocationValidators {

    public boolean hasAlgorithm(final DataDefinition dataDefinition, final Entity entity) {
        if (LocationType.WAREHOUSE.equals(LocationType.parseString(entity.getStringField(LocationFields.TYPE)))
                && StringUtils.isEmpty(entity.getStringField(LocationFieldsMFR.ALGORITHM))) {
            entity.addError(dataDefinition.getField(LocationFieldsMFR.ALGORITHM), "qcadooView.validate.field.error.missing");
            return false;
        }

        return true;
    }
}
