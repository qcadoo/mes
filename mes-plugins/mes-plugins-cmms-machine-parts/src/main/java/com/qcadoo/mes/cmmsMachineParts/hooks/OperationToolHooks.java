package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class OperationToolHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean validatesWith(final DataDefinition operationToolDD, final Entity operationTool) {
        return checkIfOperationToolExists(operationToolDD, operationTool);
    }

    private boolean checkIfOperationToolExists(final DataDefinition operationToolDD, Entity operationTool) {
        SearchCriteriaBuilder scb = operationToolDD.find();

        scb.add(SearchRestrictions.belongsTo("operation", operationTool.getBelongsToField("operation")));
        scb.add(SearchRestrictions.eq("toolCategory", operationTool.getStringField("toolCategory")));

        if (Objects.nonNull(operationTool.getBelongsToField("tool"))) {
            scb.add(SearchRestrictions.belongsTo("tool", operationTool.getBelongsToField("tool")));
        } else {
            scb.add(SearchRestrictions.isNull("tool"));
        }

        Long operationToolId = operationTool.getId();

        if (Objects.nonNull(operationToolId)) {
            scb.add(SearchRestrictions.ne("id", operationToolId));
        }

        Entity operationToolDB = scb.setMaxResults(1).uniqueResult();

        if (Objects.isNull(operationToolDB)) {
            return true;
        } else {
            if (Objects.nonNull(operationTool.getBelongsToField("tool"))) {
                operationTool.addError(operationToolDD.getField("tool"), "cmmsMachineParts.operationTool.error.operationToolAlreadyExists");
            } else {
                operationTool.addError(operationToolDD.getField("toolCategory"), "cmmsMachineParts.operationTool.error.operationToolAlreadyExists");
            }
            return false;
        }
    }

}
