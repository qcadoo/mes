package com.qcadoo.mes.cmmsMachineParts.validators;

import com.qcadoo.mes.cmmsMachineParts.SourceCostService;
import com.qcadoo.mes.cmmsMachineParts.constants.SourceCostFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service public class SourceCostValidators {

    @Autowired private SourceCostService sourceCostService;

    public boolean validatesWith(final DataDefinition sourceCostDD, final Entity sourceCost) {

        if (!sourceCost.getBooleanField(SourceCostFields.DEFAULT_COST)) {
            return true;
        }

        Entity factory = sourceCost.getBelongsToField(SourceCostFields.FACTORY);

        if (factory != null) {
            return checkDefaultsForFactory(factory, sourceCost);
        } else {
            return checkDefaults(sourceCost);
        }
    }

    private boolean checkDefaultsForFactory(final Entity factory, final Entity sourceCost) {
        Optional<Entity> optional = sourceCostService.findDefaultSourceCodeForFactory(factory);
        if (optional.isPresent()) {
            Entity sc = optional.get();

            if (sourceCost.getId() == null) {
                sourceCost.addError(sourceCost.getDataDefinition().getField(SourceCostFields.DEFAULT_COST),
                        "cmmsMachineParts.sourceCost.error.defaultExist");
                return false;
            } else if (sc.getId().equals(sourceCost.getId())) {
                return true;
            } else {
                sourceCost.addError(sourceCost.getDataDefinition().getField(SourceCostFields.DEFAULT_COST),
                        "cmmsMachineParts.sourceCost.error.defaultExist");
                return false;
            }

        } else {
            return true;
        }
    }

    private boolean checkDefaults(final Entity sourceCost) {
        Optional<Entity> optional = sourceCostService.findDefaultSourceCode();
        if (optional.isPresent()) {
            Entity sc = optional.get();

            if (sourceCost.getId() == null) {
                sourceCost.addError(sourceCost.getDataDefinition().getField(SourceCostFields.DEFAULT_COST),
                        "cmmsMachineParts.sourceCost.error.defaultExist");
                return false;
            } else if (sc.getId().equals(sourceCost.getId())) {
                return true;
            } else {
                sourceCost.addError(sourceCost.getDataDefinition().getField(SourceCostFields.DEFAULT_COST),
                        "cmmsMachineParts.sourceCost.error.defaultExist");
                return false;
            }

        } else {
            return true;
        }
    }
}
