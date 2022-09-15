package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TechnologyOperationToolHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean validatesWith(final DataDefinition technologyOperationToolDD, final Entity technologyOperationTool) {
        return checkIfTechnologyOperationToolExists(technologyOperationToolDD, technologyOperationTool);
    }

    private boolean checkIfTechnologyOperationToolExists(final DataDefinition technologyOperationToolDD, Entity technologyOperationTool) {
        SearchCriteriaBuilder scb = technologyOperationToolDD.find();

        scb.add(SearchRestrictions.belongsTo("technologyOperationComponent", technologyOperationTool.getBelongsToField("technologyOperationComponent")));
        scb.add(SearchRestrictions.eq("toolCategory", technologyOperationTool.getStringField("toolCategory")));

        if (Objects.nonNull(technologyOperationTool.getBelongsToField("tool"))) {
            scb.add(SearchRestrictions.belongsTo("tool", technologyOperationTool.getBelongsToField("tool")));
        } else {
            scb.add(SearchRestrictions.isNull("tool"));
        }

        Long technologyOperationToolId = technologyOperationTool.getId();

        if (Objects.nonNull(technologyOperationToolId)) {
            scb.add(SearchRestrictions.ne("id", technologyOperationToolId));
        }

        Entity technologyOperationToolDB = scb.setMaxResults(1).uniqueResult();

        if (Objects.isNull(technologyOperationToolDB)) {
            return true;
        } else {
            if (Objects.nonNull(technologyOperationTool.getBelongsToField("tool"))) {
                technologyOperationTool.addError(technologyOperationToolDD.getField("tool"), "cmmsMachineParts.operationTool.error.operationToolAlreadyExists");
            } else {
                technologyOperationTool.addError(technologyOperationToolDD.getField("toolCategory"), "cmmsMachineParts.operationTool.error.operationToolAlreadyExists");
            }
            return false;
        }
    }

}
