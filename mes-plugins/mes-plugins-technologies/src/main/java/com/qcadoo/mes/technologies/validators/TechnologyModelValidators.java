package com.qcadoo.mes.technologies.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyModelValidators {

    public boolean checkIfTreeOperationIsValid(final DataDefinition dataDefinition, final Entity technology) {
        if (technology != null && technology.getId() != null) {
            Entity techFromDB = technology.getDataDefinition().get(technology.getId());
            if (techFromDB != null) {
                for (Entity operationComponent : techFromDB.getTreeField("operationComponents")) {
                    if (!operationComponent.getDataDefinition().callValidators(operationComponent)) {
                        String operation = operationComponent.getBelongsToField("operation").getStringField("name");
                        technology.addGlobalError("technologies.technology.validate.global.error", operation);
                        return false;
                    }

                }
            }

        }

        return true;
    }
}
