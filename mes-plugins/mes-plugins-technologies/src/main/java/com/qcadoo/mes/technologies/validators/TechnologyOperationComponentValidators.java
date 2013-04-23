package com.qcadoo.mes.technologies.validators;

import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.REFERENCE_TECHNOLOGY;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationComponentValidators {

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_REFERENCE_MODE = "referenceMode";

    private static final String L_OPERATION = "operation";

    public boolean validate(final DataDefinition dataDefinition, final Entity technologyOperationComponent) {
        boolean isValid = true;

        // isValid = isValid && validateEntityTypeOfTechnologyOperationComponent(dataDefinition, technologyOperationComponent);
        // isValid = isValid
        // && technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(dataDefinition, technologyOperationComponent);
        return isValid;
    }

    public boolean validateEntityTypeOfTechnologyOperationComponent(final DataDefinition dataDefinition,
            final Entity technologyOperationComponent) {
        boolean isValid = true;
        if (L_OPERATION.equals(technologyOperationComponent.getStringField(L_ENTITY_TYPE))) {
            if (technologyOperationComponent.getField(L_OPERATION) == null) {
                technologyOperationComponent.addError(dataDefinition.getField(L_OPERATION),
                        "qcadooView.validate.field.error.missing");
                isValid = false;
            }
        } else if (REFERENCE_TECHNOLOGY.equals(technologyOperationComponent.getStringField(L_ENTITY_TYPE))) {
            if (technologyOperationComponent.getField(REFERENCE_TECHNOLOGY) == null) {
                technologyOperationComponent.addError(dataDefinition.getField(REFERENCE_TECHNOLOGY),
                        "qcadooView.validate.field.error.missing");
                isValid = false;
            }
            if (technologyOperationComponent.getField(L_REFERENCE_MODE) == null) {
                technologyOperationComponent.setField(L_REFERENCE_MODE, "01reference");
            }
        } else {
            throw new IllegalStateException("unknown entityType");
        }
        return isValid;
    }

}
