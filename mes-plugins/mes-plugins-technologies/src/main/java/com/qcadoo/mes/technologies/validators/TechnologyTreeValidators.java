package com.qcadoo.mes.technologies.validators;

import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;
import static com.qcadoo.mes.technologies.states.constants.TechnologyState.ACCEPTED;
import static com.qcadoo.mes.technologies.states.constants.TechnologyState.CHECKED;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Component
public class TechnologyTreeValidators {

    @Autowired
    private TechnologyTreeValidationService technologyTreeValidationService;

    public boolean checkConsumingTheSameProductFromManySubOperations(final DataDefinition dd, final Entity tech) {
        if (!(ACCEPTED.getStringValue().equals(tech.getStringField(STATE)) || CHECKED.getStringValue().equals(
                tech.getStringField(STATE)))) {
            return true;
        }

        Entity techFromDB = tech.getDataDefinition().get(tech.getId());

        EntityTree tree = techFromDB.getTreeField("operationComponents");
        Map<String, Set<Entity>> nodesMap = technologyTreeValidationService
                .checkConsumingTheSameProductFromManySubOperations(tree);

        for (Entry<String, Set<Entity>> entry : nodesMap.entrySet()) {
            String parentNodeNumber = entry.getKey();
            for (Entity product : entry.getValue()) {
                String productName = product.getStringField("name");
                String productNumber = product.getStringField("number");

                tech.addGlobalError(
                        "technologies.technology.validate.global.error.subOperationsProduceTheSameProductThatIsConsumed",
                        parentNodeNumber, productName, productNumber);
            }
        }

        return nodesMap.isEmpty();
    }
}
