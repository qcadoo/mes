package com.qcadoo.mes.productionTimeNorms.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class TechnologyModelValidators {

    public boolean checkIfAllOperationComponenthHaveTJSet(final DataDefinition dataDefinition, final Entity technology) {
        if (!"02accepted".equals(technology.getStringField("state"))) {
            return true;
        }
        MultiFieldErrorHandler errors = new MultiFieldErrorHandler();
        Entity savedTechnology = dataDefinition.get(technology.getId());
        EntityTree operationComponents = savedTechnology.getTreeField("operationComponents");

        for (Entity operationComponent : operationComponents) {
            if (!checkIfTJSet(operationComponent)) {
                StringBuilder fieldName = new StringBuilder();
                fieldName.append(operationComponent.getStringField("nodeNumber")).append(" ");
                fieldName.append(operationComponent.getBelongsToField("operation").getStringField("number")).append(" ");
                fieldName.append(operationComponent.getBelongsToField("operation").getStringField("name"));
                errors.addToErrorMessage(fieldName.toString());
            }
        }

        return errors.getMessages("technologies.technology.validate.global.error.noTJSpecified", technology);
    }

    private boolean checkIfTJSet(Entity operationComponent) {
        if (operationComponent.getField("tj") == null) {
            return false;
        }

        return true;
    }

    private class MultiFieldErrorHandler {

        private boolean hasError = false;

        private final StringBuilder errorString = new StringBuilder();

        public void addToErrorMessage(final String field) {
            errorString.append(" ").append(field).append(";");
            hasError = true;
        }

        public boolean getMessages(final String error, final Entity entity) {
            if (hasError == true) {
                entity.addGlobalError(error, errorString.toString());
                return false;
            }
            return true;
        }
    }
}
