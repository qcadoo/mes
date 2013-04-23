package com.qcadoo.mes.timeNormsForOperations.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationComponentValidatorsTNFO {

    @Autowired
    private TechnologyValidatorsServiceTNFO technologyValidatorsServiceTNFO;

    public boolean validate(final DataDefinition tocDD, final Entity technologyOperationComponent) {
        boolean isValid = true;
        isValid = isValid && technologyValidatorsServiceTNFO.checkIfUnitMatch(tocDD, technologyOperationComponent);
        isValid = isValid && technologyValidatorsServiceTNFO.checkIfUnitsInTechnologyMatch(tocDD, technologyOperationComponent);
        return isValid;
    }
}
