package com.qcadoo.mes.costCalculation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class CostCalculationModelValidators {

    @Autowired
    private TechnologyService technologyService;

    private static final Logger LOG = LoggerFactory.getLogger(CostCalculationModelValidators.class);

    public boolean checkIfTheTechnologyTreeIsntEmpty(final DataDefinition dataDefinition, final Entity costCalculation) {
        Entity technology = costCalculation.getBelongsToField("technology");
        EntityTree tree = technology.getTreeField("operationComponents");

        if (tree != null && !tree.isEmpty()) {
            try {
                for (Entity operationComponent : tree) {
                    technologyService.getProductCountForOperationComponent(operationComponent);
                }
                return true;
            } catch (IllegalStateException e) {
                LOG.debug("invalid technology tree passed to cost calculation");
            }
        }

        costCalculation.addError(dataDefinition.getField("technology"), "costNormsForOperation.messages.fail.emptyTree");
        return false;
    }
}
