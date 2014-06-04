package com.qcadoo.mes.technologies.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentReferenceMode;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationComponentValidators {

    public boolean validate(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        boolean isValid = true;

        // TODO DEV_TEAM validations can be uncommented when we fixed problem with add reference technology
        // isValid = isValid && validateEntityTypeOfTechnologyOperationComponent(technologyOperationComponentDD,
        // technologyOperationComponent);
        // isValid = isValid
        // && technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(technologyOperationComponentDD,
        // technologyOperationComponent);

        return isValid;
    }

    public boolean validateEntityTypeOfTechnologyOperationComponent(final DataDefinition technologyOperationComponentDD,
            final Entity technologyOperationComponent) {
        boolean isValid = true;

        if (TechnologyOperationComponentType.OPERATION.getStringValue().equals(
                technologyOperationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
            if (technologyOperationComponent.getField(TechnologyOperationComponentFields.OPERATION) == null) {
                technologyOperationComponent.addError(
                        technologyOperationComponentDD.getField(TechnologyOperationComponentFields.OPERATION),
                        "qcadooView.validate.field.error.missing");

                isValid = false;
            }
        } else if (TechnologyOperationComponentType.REFERENCE_TECHNOLOGY.getStringValue().equals(
                technologyOperationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
            if (technologyOperationComponent.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY) == null) {
                technologyOperationComponent.addError(
                        technologyOperationComponentDD.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY),
                        "qcadooView.validate.field.error.missing");

                isValid = false;
            }
            if (technologyOperationComponent.getField(TechnologyOperationComponentFields.REFERENCE_MODE) == null) {
                technologyOperationComponent.setField(TechnologyOperationComponentFields.REFERENCE_MODE,
                        TechnologyOperationComponentReferenceMode.REFERENCE.getStringValue());
            }
        } else {
            throw new IllegalStateException("unknown entityType");
        }

        return isValid;
    }

}
