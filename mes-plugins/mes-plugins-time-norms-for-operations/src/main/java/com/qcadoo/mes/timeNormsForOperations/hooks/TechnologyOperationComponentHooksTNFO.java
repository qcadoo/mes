package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION;
import static com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_OPERATION;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationComponentHooksTNFO {

    public void copyTimeNormsToTechnologyOperationComponent(final DataDefinition dd, final Entity technologyOperationComponent) {
        if ("referenceTechnology".equals(technologyOperationComponent.getField("entityType"))) {
            return;
        }
        if (technologyOperationComponent.getBelongsToField(OPERATION) == null) {
            return;
        }
        copyTimeValuesFromGivenOperation(technologyOperationComponent, technologyOperationComponent.getBelongsToField(OPERATION));
    }

    private void copyTimeValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(target != null, "given target is null");
        checkArgument(source != null, "given source is null");

        if (!shouldPropagateValuesFromLowerInstance(target)) {
            return;
        }

        for (String fieldName : FIELDS_OPERATION) {
            if (source.getField(fieldName) == null) {
                continue;
            }
            target.setField(fieldName, source.getField(fieldName));
        }
    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity technologyOperationComponent) {
        for (String fieldName : FIELDS_OPERATION) {
            if (technologyOperationComponent.getField(fieldName) != null) {
                return false;
            }
        }
        return true;
    }
}
